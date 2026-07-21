package com.shea.agent.interviewagent.vectorstore;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.shea.agent.interviewagent.constant.Constant.FILE_PATH_PREFIX;

/**
 * @author : Shea.
 * @since : 2026/7/19 20:37
 */
@Configuration
@Slf4j
public class PgVectorStoreConfig {

    private static final String FILE_PATH = FILE_PATH_PREFIX + "documents";

    @Value("${rag.autoLoad:false}")
    private boolean autoLoadDocument;

    @Value("${rag.batchSize:10}")
    private int batchSize;

    @Value("${rag.filePattern:*.md}")
    private String filePattern;

    @jakarta.annotation.Resource
    private TransactionTemplate transactionTemplate;

    @Bean
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashScopeEmbeddingModel) throws IOException {
        return PgVectorStore
                .builder(jdbcTemplate, dashScopeEmbeddingModel)
                .initializeSchema(true)
                .build();
    }

    @Bean
    public ApplicationRunner vectorStoreInitializer(
            JdbcTemplate jdbcTemplate,
            VectorStore vectorStore,
            @Autowired(required = false) EmbeddingModel embeddingModel) {
        return args -> {
            if (autoLoadDocument && embeddingModel != null) {
                // 异步执行，不阻塞启动
                CompletableFuture.runAsync(() -> {
                    try {
                        log.info("开始异步加载文档到向量库...");
                        syncDocuments(vectorStore, jdbcTemplate);
                        log.info("文档加载完成");
                    } catch (Exception e) {
                        log.error("文档加载失败", e);
                    }
                });
            }
        };
    }

    /**
     * 同步文档主流程
     */
    protected void syncDocuments(VectorStore vectorStore, JdbcTemplate jdbcTemplate) throws IOException {
        List<Document> documents = loadDocuments();
        if (documents.isEmpty()) {
            log.warn("未找到任何文档");
            return;
        }

        Map<String, FileDocumentInfo> existingDocs = queryExistingDocs(jdbcTemplate);
        Map<String, List<Document>> chunksByFile = documents.stream()
                .collect(Collectors.groupingBy(d -> (String) d.getMetadata().get("filename")));

        for (Map.Entry<String, List<Document>> entry : chunksByFile.entrySet()) {
            String filename = entry.getKey();
            List<Document> fileChunks = entry.getValue();

            // 获取文件级别的 hash
            String newFileHash = (String) fileChunks.getFirst().getMetadata().get("file_hash");

            // 检查是否需要更新
            FileDocumentInfo existing = existingDocs.get(filename);
            if (existing != null && newFileHash != null && newFileHash.equals(existing.fileHash)) {
                log.info("文档 [{}] 未发生变化，跳过同步", filename);
                continue;
            }

            // 在事务中删除和新增
            transactionTemplate.execute(status -> {
                try {
                    if (existing != null && CollUtil.isNotEmpty(existing.ids)) {
                        vectorStore.delete(existing.ids);
                        log.info("删除旧文档 [{}] 的 {} 个 chunk", filename, existing.ids.size());
                    }
                    addDocumentsWithTransaction(vectorStore, fileChunks);
                    log.info("入库文档 [{}]，{} 个 chunk", filename, fileChunks.size());
                } catch (Exception e) {
                    log.error("同步文档 [{}] 失败", filename, e);
                    throw new RuntimeException("同步文档失败: " + filename, e);
                }
                return true;
            });
        }
    }

    /**
     * 批量添加文档，使用事务保证原子性
     */
    protected void addDocumentsWithTransaction(VectorStore vectorStore, List<Document> fileChunks) {
        // 分批处理，但整个文件在一个事务中
        for (int i = 0; i < fileChunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, fileChunks.size());
            List<Document> batch = fileChunks.subList(i, end);
            vectorStore.add(batch);
            log.debug("已添加 {}/{} 个 chunk", end, fileChunks.size());
        }
    }

    /**
     * 加载文档并生成文件级和 chunk 级 hash
     */
    private List<Document> loadDocuments() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String pathPattern = FILE_PATH + "/" + filePattern;
        Resource[] resources = resolver.getResources(pathPattern);

        if (resources.length == 0) {
            log.warn("未找到匹配的文档: {}", pathPattern);
            return Collections.emptyList();
        }

        RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter();
        List<Document> allDocuments = new ArrayList<>();

        for (Resource resource : resources) {
            String filename = resource.getFilename() == null ? "未命名" : resource.getFilename();

            try {
                List<Document> chunkedDocuments = processSingleFile(resource, filename, splitter);
                allDocuments.addAll(chunkedDocuments);
                log.info("加载文档 [{}]，共 {} 个 chunk", filename, chunkedDocuments.size());
            } catch (Exception e) {
                log.error("加载文档 [{}] 失败", filename, e);
                // 单个文件失败不影响其他文件
            }
        }

        return allDocuments;
    }

    /**
     * 处理单个文件，生成 chunk 和 hash
     */
    private List<Document> processSingleFile(Resource resource, String filename,
                                             RecursiveCharacterTextSplitter splitter) throws IOException {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("filename", filename)
                .build();

        MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
        List<Document> documents = reader.read();

        // 计算文件级别的 hash（基于所有文本内容）
        String fullText = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining());
        String fileHash = DigestUtil.md5Hex(fullText);

        // 分块并添加 hash
        return splitter.split(documents).stream()
                .peek(doc -> {
                    // chunk 级别的 hash
                    String chunkHash = DigestUtil.md5Hex(doc.getText());
                    doc.getMetadata().put("chunk_hash", chunkHash);
                    // 文件级别的 hash
                    doc.getMetadata().put("file_hash", fileHash);
                })
                .toList();
    }

    /**
     * 查询向量库已有文档，按 filename 分组
     */
    private Map<String, FileDocumentInfo> queryExistingDocs(JdbcTemplate jdbcTemplate) {
        Map<String, FileDocumentInfo> result = new HashMap<>();

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, metadata->>'file_hash' as file_hash, metadata->>'filename' as filename " +
                            "FROM vector_store"
            );

            for (Map<String, Object> row : rows) {
                String filename = (String) row.get("filename");
                if (filename == null) continue;

                String fileHash = (String) row.get("file_hash");
                String id = row.get("id").toString();

                result.computeIfAbsent(filename, k -> new FileDocumentInfo())
                        .addId(id)
                        .setFileHash(fileHash);
            }

            log.info("查询到 {} 个已有文档", result.size());

        } catch (BadSqlGrammarException e) {
            // 表不存在，正常情况
            log.info("向量表尚未创建，首次启动");
        } catch (Exception e) {
            log.error("查询向量库失败", e);
            throw new RuntimeException("查询向量库失败", e);
        }

        return result;
    }

    /**
     * 文档信息内部类
     */
    @Data
    private static class FileDocumentInfo {
        private String fileHash;
        private final List<String> ids = new ArrayList<>();

        public FileDocumentInfo addId(String id) {
            this.ids.add(id);
            return this;
        }

        public List<String> getIds() {
            return Collections.unmodifiableList(ids);
        }
    }

}
