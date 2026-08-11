package com.shea.agent.interviewagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.entity.ResumeInfo;
import com.shea.agent.interviewagent.vo.ResumeInfoVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Shea
 * @since 2026-08-05
 */
public interface IResumeInfoService extends IService<ResumeInfo> {

    Boolean saveResume(ResumeInfoDTO dto, Long userId);

    List<ResumeInfoVO> listResumeInfo(Long userId);
}
