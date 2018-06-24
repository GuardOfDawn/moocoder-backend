package com.moekr.moocoder.web.controller.api;

import com.moekr.moocoder.logic.service.ResultService;
import com.moekr.moocoder.util.exceptions.AccessDeniedException;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.response.ResourceResponse;
import com.moekr.moocoder.web.response.Response;
import com.moekr.moocoder.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

import static com.moekr.moocoder.web.security.WebSecurityConstants.*;

@RestController
@RequestMapping("/api")
public class ResultController extends AbstractApiController {
	private final ResultService resultService;

	@Autowired
	public ResultController(ResultService resultService) {
		this.resultService = resultService;
	}

	@GetMapping("/result/{resultId:\\d+}")
	public Response retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
							 @PathVariable int resultId) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new ResourceResponse(resultService.retrieve(resultId));
		}
		return new ResourceResponse(resultService.retrieve(userDetails.getId(), resultId));
	}

	@GetMapping({"/result", "/user/{userId:\\d+}/result"})
	@RolesAllowed({STUDENT_ROLE, ADMIN_ROLE})
	public Response retrieveByOwner(@AuthenticationPrincipal CustomUserDetails userDetails,
									@PathVariable(required = false) Integer userId) throws ServiceException {
		if (userDetails.isAdmin() && userId != null) {
			return new ResourceResponse(resultService.retrieveByOwner(userId));
		} else if (userId == null) {
			return new ResourceResponse(resultService.retrieveByOwner(userDetails.getId()));
		}
		throw new AccessDeniedException();
	}

	@GetMapping("/exam/{examId:\\d+}/result")
	@RolesAllowed({TEACHER_ROLE, ADMIN_ROLE})
	public Response retrieveByExam(@AuthenticationPrincipal CustomUserDetails userDetails,
								   @PathVariable int examId) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new ResourceResponse(resultService.retrieveByExam(examId));
		}
		return new ResourceResponse(resultService.retrieveByExam(userDetails.getId(), examId));
	}
}
