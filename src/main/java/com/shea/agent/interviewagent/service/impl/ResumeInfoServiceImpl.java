package com.shea.agent.interviewagent.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.entity.ResumeInfo;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.mapper.ResumeInfoMapper;
import com.shea.agent.interviewagent.service.IResumeInfoService;
import com.shea.agent.interviewagent.vo.ResumeInfoVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Shea
 * @since 2026-08-05
 */
@Service
public class ResumeInfoServiceImpl extends ServiceImpl<ResumeInfoMapper, ResumeInfo> implements IResumeInfoService {

    @Override
    public Boolean saveResume(ResumeInfoDTO dto, Long userId) {
        ResumeInfo resumeInfo = new ResumeInfo();
        resumeInfo.setName(dto.getName());
        resumeInfo.setJob(dto.getJob());
        resumeInfo.setSkills(dto.getSkills());
        resumeInfo.setWorkExperience(dto.getWorkExperiences());
        resumeInfo.setProject(dto.getProjects());
        resumeInfo.setUserId(userId);
        try{
            return this.save(resumeInfo);
        }catch (DuplicateKeyException e){
            throw new BusinessException("一个用户只能上传一份简历，请先删除之前上传的简历");
        }
    }

    @Override
    public List<ResumeInfoVO> listResumeInfo(Long userId) {
        LambdaQueryWrapper<ResumeInfo> select = Wrappers.lambdaQuery(ResumeInfo.class)
                .eq(ResumeInfo::getUserId, userId)
                .orderByDesc(ResumeInfo::getCreateTime)
                .select(ResumeInfo::getId,
                        ResumeInfo::getUserId,
                        ResumeInfo::getName,
                        ResumeInfo::getJob,
                        ResumeInfo::getSkills,
                        ResumeInfo::getProject,
                        ResumeInfo::getWorkExperience,
                        ResumeInfo::getProject
                );
        List<ResumeInfo> infos = this.list(select);
        return JSONUtil.toList(JSONUtil.toJsonStr(infos), ResumeInfoVO.class);
    }
}
