package com.moekr.moocoder.logic.api;

import com.moekr.moocoder.logic.api.vo.GitlabUser;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.CommitAction;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

public interface GitlabApi {
	GitlabUser createUser(String username, String email, String password) throws GitLabApiException;

	Integer createProject(String name) throws GitLabApiException;

	Integer forkProject(int userId, int projectId, int namespaceId) throws GitLabApiException;

	Set<String> compare(int projectId, String from, String to) throws GitLabApiException;

	void archiveProject(int projectId) throws GitLabApiException;

	void changePassword(int userId, String password) throws GitLabApiException;

	void deleteUser(int userId) throws GitLabApiException;

	void deleteProject(int projectId) throws GitLabApiException;

	/**
	 * get file content
	 * @param projectId
	 * @param branch
	 * @param path
	 * @return
	 */
	String getRepositoryFile(int projectId, String branch, String path) throws GitLabApiException, UnsupportedEncodingException;

	/**
	 * create a commit
	 * @param projectId
	 * @param branch
	 * @param commitMessage
	 * @param email
	 * @param username
	 * @param actions
	 */
	void createCommit(int projectId, String branch, String commitMessage, String email, String username, List<CommitAction> actions) throws GitLabApiException;

}
