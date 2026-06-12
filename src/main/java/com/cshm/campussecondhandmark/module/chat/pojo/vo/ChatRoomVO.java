package com.cshm.campussecondhandmark.module.chat.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "聊天房间信息")
public class ChatRoomVO {

    @ApiModelProperty(value = "Matrix 房间 ID，如 !abc123:localhost")
    private String matrixRoomId;
}
