package com.shea.agent.interviewagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.entity.ResumeInfo;
import com.shea.agent.interviewagent.mapper.ResumeInfoMapper;
import com.shea.agent.interviewagent.service.IResumeInfoService;
import org.springframework.stereotype.Service;

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
    public Boolean saveResume(ResumeInfoDTO dto) {
        ResumeInfo resumeInfo = new ResumeInfo();
        resumeInfo.setName(dto.getName());
        resumeInfo.setJob(dto.getJob());
        resumeInfo.setSkills(dto.getSkills());
        resumeInfo.setWorkExperience(dto.getWorkExperiences());
        resumeInfo.setProject(dto.getProjects());
        resumeInfo.setUserId(1L);
        return this.save(resumeInfo);
    }
}
