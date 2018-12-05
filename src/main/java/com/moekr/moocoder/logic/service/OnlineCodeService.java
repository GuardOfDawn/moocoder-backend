package com.moekr.moocoder.logic.service;

import com.moekr.moocoder.logic.vo.CodeVO;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.CodeDTO;

public interface OnlineCodeService {
    CodeVO getCode(int userId, int examId, int questionId) throws ServiceException;

    void submitCode(int userId, int examId, int questionId, CodeDTO codeDTO) throws ServiceException;
}
