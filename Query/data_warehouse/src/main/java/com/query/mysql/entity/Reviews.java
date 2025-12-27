package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>
 * 电影评论表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("reviews")
public class Reviews implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论 UUID（唯一）
     */
    private byte[] reviewUuid;

    /**
     * 电影 ID（外键 movies.id）
     */
    private Long movieId;

    /**
     * 有用票数，如 2/3
     */
    private String helpfulness;

    /**
     * 评论者昵称
     */
    private String profileName;

    /**
     * 评分（1-5）
     */
    private Byte score;

    /**
     * 评论时间戳
     */
    private Long createdTs;

    /**
     * 评论摘要
     */
    private String summary;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者用户ID
     */
    private String userId;
}
