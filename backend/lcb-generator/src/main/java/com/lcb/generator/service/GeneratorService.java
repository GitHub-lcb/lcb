package com.lcb.generator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lcb.generator.domain.GenTable;
import com.lcb.generator.domain.GenTableColumn;
import com.lcb.generator.mapper.GenTableColumnMapper;
import com.lcb.generator.mapper.GenTableMapper;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class GeneratorService {

    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper genTableColumnMapper;
    private VelocityEngine velocityEngine;

    @Value("${generator.backend-path:./backend}")
    private String backendPath;

    @Value("${generator.frontend-path:./frontend}")
    private String frontendPath;

    public GeneratorService(GenTableMapper genTableMapper, GenTableColumnMapper genTableColumnMapper) {
        this.genTableMapper = genTableMapper;
        this.genTableColumnMapper = genTableColumnMapper;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();
    }

    public Page<GenTable> selectTablePage(Page<GenTable> page) {
        return genTableMapper.selectPage(page, null);
    }

    public GenTable getTableById(Long tableId) {
        return genTableMapper.selectById(tableId);
    }

    public List<Map<String, Object>> getDbTables() {
        return genTableMapper.selectDbTables();
    }

    public List<Map<String, Object>> getDbColumns(String tableName) {
        return genTableMapper.selectDbColumns(tableName);
    }

    @Transactional
    public void importTable(String tableName) {
        Map<String, Object> tableInfo = genTableMapper.selectDbTables().stream()
            .filter(t -> t.get("table_name").equals(tableName))
            .findFirst().orElseThrow(() -> new RuntimeException("表不存在: " + tableName));

        GenTable genTable = new GenTable();
        genTable.setTableName(tableName);
        genTable.setTableComment((String) tableInfo.get("table_comment"));
        genTable.setClassName(toClassName(tableName));
        genTable.setModuleName("system");
        genTable.setPackageName("com.lcb.system");
        genTable.setTplCategory("crud");
        genTableMapper.insert(genTable);

        List<Map<String, Object>> columns = genTableMapper.selectDbColumns(tableName);
        for (Map<String, Object> col : columns) {
            GenTableColumn column = new GenTableColumn();
            column.setTableId(genTable.getId());
            column.setColumnName((String) col.get("column_name"));
            column.setColumnComment((String) col.get("column_comment"));
            column.setJavaType(mapJavaType((String) col.get("data_type")));
            column.setJavaField(toJavaField((String) col.get("column_name")));
            column.setIsInsert(1);
            column.setIsEdit(1);
            column.setIsList(1);
            column.setQueryType("EQ");
            genTableColumnMapper.insert(column);
        }
    }

    public void generateCode(Long tableId) {
        GenTable table = genTableMapper.selectById(tableId);
        String className = table.getClassName();
        String classNameLower = Character.toLowerCase(className.charAt(0)) + className.substring(1);
        String moduleName = table.getModuleName();
        String packageName = table.getPackageName().replace(".", "/");
        String packagePath = "com/" + packageName;

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("className", className);
        ctx.put("classNameLower", classNameLower);
        ctx.put("moduleName", moduleName);
        ctx.put("packageName", table.getPackageName());
        ctx.put("tableComment", table.getTableComment());

        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("controller.java.vm", backendPath + "/lcb-" + moduleName + "/src/main/java/" + packagePath + "/controller/" + className + "Controller.java");
        templates.put("entity.java.vm", backendPath + "/lcb-" + moduleName + "/src/main/java/" + packagePath + "/domain/" + className + ".java");
        templates.put("mapper.java.vm", backendPath + "/lcb-" + moduleName + "/src/main/java/" + packagePath + "/mapper/" + className + "Mapper.java");
        templates.put("service.java.vm", backendPath + "/lcb-" + moduleName + "/src/main/java/" + packagePath + "/service/I" + className + "Service.java");
        templates.put("serviceImpl.java.vm", backendPath + "/lcb-" + moduleName + "/src/main/java/" + packagePath + "/service/impl/" + className + "ServiceImpl.java");
        templates.put("vue.vue.vm", frontendPath + "/src/pages/" + moduleName + "/" + classNameLower + "/index.vue");
        templates.put("api.ts.vm", frontendPath + "/src/api/" + moduleName + "/" + classNameLower + ".ts");

        for (Map.Entry<String, String> entry : templates.entrySet()) {
            try {
                org.apache.velocity.Template tpl = velocityEngine.getTemplate("templates/" + entry.getKey(), "UTF-8");
                VelocityContext context = new VelocityContext(new HashMap<>(ctx));
                StringWriter sw = new StringWriter();
                tpl.merge(context, sw);
                Path targetFile = Paths.get(entry.getValue());
                Files.createDirectories(targetFile.getParent());
                Files.writeString(targetFile, sw.toString());
            } catch (Exception e) {
                throw new RuntimeException("生成代码失败: " + entry.getKey(), e);
            }
        }
    }

    private String toClassName(String tableName) {
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for (char c : tableName.toCharArray()) {
            if (c == '_') { upper = true; continue; }
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = false;
        }
        String result = sb.toString();
        // Remove table prefix (t_sys -> Sys, t_gen -> Gen)
        if (result.startsWith("T")) {
            result = result.substring(1);
        }
        return result;
    }

    private String toJavaField(String columnName) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : columnName.toCharArray()) {
            if (c == '_') { upper = true; continue; }
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = false;
        }
        return sb.toString();
    }

    private String mapJavaType(String dbType) {
        return switch (dbType.toUpperCase()) {
            case "VARCHAR", "CHAR", "TEXT", "LONGTEXT" -> "String";
            case "INT", "TINYINT", "SMALLINT" -> "Integer";
            case "BIGINT" -> "Long";
            case "DATETIME", "TIMESTAMP", "DATE" -> "LocalDateTime";
            case "DECIMAL", "DOUBLE", "FLOAT" -> "BigDecimal";
            default -> "String";
        };
    }
}
