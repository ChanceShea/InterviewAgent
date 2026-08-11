package com.shea.agent.interviewagent.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.entity.ResumeInfo;
import com.shea.agent.interviewagent.mapper.ResumeInfoMapper;
import com.shea.agent.interviewagent.service.IResumeInfoService;
import com.shea.agent.interviewagent.vo.ResumeInfoVO;
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
        return this.save(resumeInfo);
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
