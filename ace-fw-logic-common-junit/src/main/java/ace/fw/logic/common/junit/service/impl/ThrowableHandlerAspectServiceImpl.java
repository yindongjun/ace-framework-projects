package ace.fw.logic.common.junit.service.impl;

import ace.fw.exception.BusinessException;
import ace.fw.logic.common.aop.Interceptor.handler.annotations.ThrowableHandlerAspect;
import ace.fw.logic.common.aop.Interceptor.log.annotations.LogAspect;
import ace.fw.logic.common.junit.model.bo.UserBo;
import ace.fw.logic.common.junit.model.request.FindByIdRequest;
import ace.fw.logic.common.junit.service.MethodLogAspectService;
import ace.fw.logic.common.junit.service.ThrowableHandlerAspectService;
import ace.fw.model.response.GenericResponseExt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author Caspar
 * @contract 279397942@qq.com
 * @create 2020/7/29 15:41
 * @description
 */
@Component
@Slf4j
public class ThrowableHandlerAspectServiceImpl implements ThrowableHandlerAspectService {

    private final static String ID_1 = "1";
    private final static String ID_2 = "2";

    private List<UserBo> userList = Arrays.asList(
            UserBo.builder()
                    .createTime(LocalDateTime.now())
                    .id(ID_1)
                    .nickName("1")
                    .state(1)
                    .version(1)
                    .build(),
            UserBo.builder()
                    .createTime(LocalDateTime.now())
                    .id(ID_2)
                    .nickName("2")
                    .state(1)
                    .version(1)
                    .build()
    );


    private UserBo cloneUserBo(UserBo userBo) {
        return UserBo.builder().version(userBo.getVersion())
                .state(userBo.getState())
                .nickName(userBo.getNickName())
                .id(userBo.getId())
                .createTime(userBo.getCreateTime())
                .build();
    }

    @LogAspect
    @ThrowableHandlerAspect
    @Override
    public GenericResponseExt<UserBo> testThrowException(@Valid FindByIdRequest request) {
        int i = 1 / (1 - 1);
        return null;
    }

    @LogAspect
    @ThrowableHandlerAspect
    @Override
    public GenericResponseExt testThrowBusiness() {
        throw new BusinessException("1000", "business");
    }

    @LogAspect
    @ThrowableHandlerAspect
    @Override
    public GenericResponseExt testThrowValidationException(@Valid FindByIdRequest request) throws BindException {

        throw new BindException(this, "aa");


    }
}
