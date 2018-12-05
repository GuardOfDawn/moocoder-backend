package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.ExamDAO;
import com.moekr.moocoder.data.dao.ResultDAO;
import com.moekr.moocoder.data.dao.UserDAO;
import com.moekr.moocoder.data.entity.Exam;
import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.logic.api.GitlabApi;
import com.moekr.moocoder.logic.service.OnlineCodeService;
import com.moekr.moocoder.logic.vo.CodeVO;
import com.moekr.moocoder.util.ApplicationProperties;
import com.moekr.moocoder.util.ToolKit;
import com.moekr.moocoder.util.exceptions.Asserts;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.CodeDTO;
import lombok.extern.apachecommons.CommonsLog;
import org.gitlab4j.api.CommitsApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.RepositoryFileApi;
import org.gitlab4j.api.models.CommitAction;
import org.gitlab4j.api.models.RepositoryFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
@CommonsLog
public class OnlineCodeServiceImpl implements OnlineCodeService {
    private final UserDAO userDAO;
    private final ExamDAO examDAO;
    private final ResultDAO resultDAO;
    private final GitlabApi gitlabApi;

    @Autowired
    public OnlineCodeServiceImpl(UserDAO userDAO, ExamDAO examDAO, ResultDAO resultDAO, GitlabApi gitlabApi){
        this.userDAO = userDAO;
        this.examDAO = examDAO;
        this.resultDAO = resultDAO;
        this.gitlabApi = gitlabApi;
    }

    @Override
    public CodeVO getCode(int userId, int examId, int questionId) throws ServiceException {
        Exam exam = examDAO.findById(examId);
        Asserts.notNull(exam, "所选考试不存在");
        User user = userDAO.findById(userId);
        Result result = resultDAO.findByOwnerAndExam(user, exam);
        Asserts.notNull(result, "不存在该场考试的记录");
        Problem problem = null;
        for(Problem p:exam.getProblems()){
            if(p.getId()==questionId){
                problem = p;
                break;
            }
        }
        Asserts.notNull(problem, "所选试题不存在");
        Map<String, String> editable = new HashMap<>();
        for(String filepath:problem.getPublicFiles()){
            try {
                String fullFilepath = "/" + problem.getUniqueName() + filepath;
                String fileContent = gitlabApi.getRepositoryFile(result.getId(), "master", fullFilepath);
                editable.put(filepath, fileContent);
            } catch (GitLabApiException | UnsupportedEncodingException e) {
                throw new ServiceException("从git获取题目时发生异常" + ToolKit.format(e));
            }
        }
        Map<String, String> uneditable = new HashMap<>();
        for(String filepath:problem.getProtectedFiles()){
            if(filepath.contains(".gitignore")||filepath.contains("pom.xml")||filepath.contains("requirements.txt"))
                continue;
            try {
                String fullFilepath = "/" + problem.getUniqueName() + filepath;
                String fileContent = gitlabApi.getRepositoryFile(result.getId(), "master", fullFilepath);
                uneditable.put(filepath, fileContent);
            } catch (GitLabApiException | UnsupportedEncodingException e) {
                throw new ServiceException("从git获取题目时发生异常" + ToolKit.format(e));
            }
        }
        CodeVO codeVO = new CodeVO(examId, exam.getEndAt(), questionId, problem.getType().getLanguage(), problem.getUniqueName(), editable, uneditable);
        return codeVO;
    }

    @Override
    public void submitCode(int userId, int examId, int questionId, CodeDTO codeDTO) throws ServiceException {
        Exam exam = examDAO.findById(examId);
        Asserts.notNull(exam, "所选考试不存在");
        User user = userDAO.findById(userId);
        Result result = resultDAO.findByOwnerAndExam(user, exam);
        Asserts.notNull(result, "不存在该场考试的记录");
        Problem problem = null;
        for(Problem p:exam.getProblems()){
            if(p.getId()==questionId){
                problem = p;
                break;
            }
        }
        Asserts.notNull(problem, "所提交试题不存在");
        try {
            List<CommitAction> actions = new ArrayList<>();
            for(Map.Entry<String, String> entry:codeDTO.getCode().entrySet()){
                CommitAction action = new CommitAction();
                action.setAction(CommitAction.Action.UPDATE);
                action.setEncoding(CommitAction.Encoding.TEXT);
                action.setFilePath("/" + problem.getUniqueName() + entry.getKey());
                action.setContent(entry.getValue());
                actions.add(action);
            }
            gitlabApi.createCommit(result.getId(), "master", "commit", user.getEmail(), user.getUsername(),actions);
        } catch (GitLabApiException e) {
            throw new ServiceException("向git提交题目时发生异常" + ToolKit.format(e));
        }
    }
}
