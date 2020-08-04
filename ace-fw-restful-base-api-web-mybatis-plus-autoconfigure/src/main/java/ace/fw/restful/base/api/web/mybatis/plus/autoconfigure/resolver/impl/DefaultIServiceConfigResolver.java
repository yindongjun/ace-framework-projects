package ace.fw.restful.base.api.web.mybatis.plus.autoconfigure.resolver.impl;

import ace.fw.data.model.EntityInfo;
import ace.fw.data.model.EntityProperty;
import ace.fw.data.model.entity.Entity;
import ace.fw.exception.SystemException;
import ace.fw.mybatis.plus.extension.model.EntityServiceInfo;
import ace.fw.mybatis.plus.extension.service.MybatisPlusDbService;
import ace.fw.mybatis.plus.extension.service.impl.MybatisPlusDbServiceImpl;
import ace.fw.restful.base.api.web.mybatis.plus.autoconfigure.resolver.IServiceConfigResolver;
import ace.fw.util.ReflectionUtils;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Caspar
 * @contract 279397942@qq.com
 * @create 2020/1/3 14:30
 * @description {@link MybatisPlusDbService} 解析系统业务接口，匹配通用接口,默认解析实现{@link MybatisPlusDbServiceImpl}
 */
@Data
@Accessors(chain = true)
public class DefaultIServiceConfigResolver implements IServiceConfigResolver {

    private List<MybatisPlusDbService> mybatisPlusDbServices;


    /**
     * 根据泛型逻辑接口，解析对应的实体信息
     *
     * @return
     */
    @Override
    public Map<String, EntityServiceInfo<Entity>> resolve() {
        if (CollectionUtils.isEmpty(mybatisPlusDbServices)) {
            return Collections.EMPTY_MAP;
        }
        Map<String, EntityServiceInfo<Entity>> entityServiceInfoMap = mybatisPlusDbServices.stream()
                .map(service -> {
                    Class entityClass = ReflectionUtils.getClassGeneric(service.getClass().getSuperclass(), 0);

                    String entityId = entityClass.getSimpleName();
                    EntityInfo entityInfo = this.resolveEntityInfo(entityId, entityClass);

                    return new EntityServiceInfo()
                            .setId(entityId)
                            .setEntityClass(entityClass)
                            .setMybatisPlusDbService(service)
                            .setEntityInfo(entityInfo);
                })
                .collect(Collectors.toMap(EntityServiceInfo::getId, a -> a));
        return entityServiceInfoMap;
    }

    /**
     * 解析实体信息
     *
     * @param entityId
     * @param entityClass
     * @return
     */
    private EntityInfo resolveEntityInfo(String entityId, Class entityClass) {
        String entityRemark = this.resolveEntityRemark(entityClass);
        List<EntityProperty> properties = this.resolveEntityProperties(entityClass);

        return EntityInfo.builder()
                .id(entityId)
                .remark(entityRemark)
                .properties(properties)
                .build();
    }

    /**
     * 解析实体属性信息
     *
     * @param entityClass
     * @return
     */
    private List<EntityProperty> resolveEntityProperties(Class entityClass) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        return Arrays.asList(entityClass.getDeclaredFields())
                .stream()
                .filter(p -> Modifier.isPrivate(p.getModifiers()) || Modifier.isProtected(p.getModifiers()))
                .map(field -> {
                    String propertyId = field.getName();
                    String propertyRemark = this.resolveEntityPropertyRemark(field);
                    Class fieldClass = field.getType();
                    String column = StringUtils.EMPTY;
                    if (StringUtils.equalsIgnoreCase(tableInfo.getKeyProperty(), propertyId)) {
                        column = tableInfo.getKeyColumn();
                    }
                    if (StringUtils.isEmpty(column)) {
                        TableFieldInfo tableFieldInfo = tableInfo.getFieldList()
                                .stream()
                                .filter(p -> StringUtils.equalsIgnoreCase(p.getProperty(), propertyId))
                                .findFirst()
                                .orElse(null);
                        if (Objects.nonNull(tableFieldInfo)) {
                            column = tableFieldInfo.getColumn();
                        }
                    }
                    if (StringUtils.isEmpty(column)) {
                        throw new SystemException(String.format("%s 无法查询对应的column", propertyId));
                    }
                    return EntityProperty.builder()
                            .id(propertyId)
                            .remark(propertyRemark)
                            .clazz(fieldClass)
                            .column(column)
                            .build();
                })
                .collect(Collectors.toList());

    }

    /**
     * 解析实体属性备注信息
     *
     * @param field
     * @return
     */
    private String resolveEntityPropertyRemark(Field field) {
        ApiModelProperty apiModelPropertyAnnotation = field.getDeclaredAnnotation(ApiModelProperty.class);
        if (apiModelPropertyAnnotation == null) {
            return "";
        }
        return apiModelPropertyAnnotation.value();
    }

    private String resolveEntityRemark(Class entityClass) {
        Annotation annotation = entityClass.getDeclaredAnnotation(ApiModel.class);
        if (annotation == null) {
            return "";
        }
        ApiModel apiModelAnnotation = (ApiModel) annotation;
        String entityRemark = apiModelAnnotation.description();
        return entityRemark;

    }
}
