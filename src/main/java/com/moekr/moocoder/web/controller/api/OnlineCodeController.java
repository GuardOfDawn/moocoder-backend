package com.moekr.moocoder.web.controller.api;

import com.moekr.moocoder.logic.service.OnlineCodeService;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.CodeDTO;
import com.moekr.moocoder.web.response.EmptyResponse;
import com.moekr.moocoder.web.response.ResourceResponse;
import com.moekr.moocoder.web.response.Response;
import com.moekr.moocoder.web.security.impl.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OnlineCodeController extends AbstractApiController {
    private final OnlineCodeService onlineCodeService;

    public OnlineCodeController(OnlineCodeService onlineCodeService){
        this.onlineCodeService = onlineCodeService;
    }

    @GetMapping("/exam/{examId:\\d+}/onlinecode/{questionId:\\d+}")
    public Response getCode(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @PathVariable int examId, @PathVariable int questionId) throws ServiceException {
        return new ResourceResponse(onlineCodeService.getCode(userDetails.getId(), examId, questionId));
    }

    @PostMapping("/exam/{examId:\\d+}/onlinecode/{questionId:\\d+}")
    public Response submitCode(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable int examId, @PathVariable int questionId,
                               @RequestBody @Validated(PutMapping.class) CodeDTO codeDTO, Errors errors) throws ServiceException {
        checkErrors(errors);
        onlineCodeService.submitCode(userDetails.getId(), examId, questionId, codeDTO);
        return new EmptyResponse();
    }
}
