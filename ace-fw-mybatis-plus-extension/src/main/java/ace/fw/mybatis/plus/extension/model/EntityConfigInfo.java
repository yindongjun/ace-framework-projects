package ace.fw.mybatis.plus.extension.model;

import ace.fw.data.model.EntityInfo;
import ace.fw.data.model.entity.Entity;
import ace.fw.mybatis.plus.extension.service.MybatisPlusDbService;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Caspar
 * @contract 279397942@qq.com
 * @create 2020/1/2 18:29
 * @description
 */
@Data
@Accessors(chain = true)
public class EntityConfigInfo<M extends Entity> {
    private String id;
    private Class<M> entityClass;
    private EntityInfo entityInfo;
}