/**
 * norm4j skill helpers
 * 
 * This file contains utility functions for working with norm4j
 */

const fs = require('fs');
const path = require('path');

/**
 * Check if a class is a valid norm4j entity
 * 
 * @param {string} classContent - Java class content
 * @returns {Object} Validation result
 */
exports.isValidEntity = function(classContent) {
    const hasTableAnnotation = classContent.includes('@Table');
    const hasIdAnnotation = classContent.includes('@Id');
    const hasPackage = classContent.includes('package ');
    
    return {
        valid: hasTableAnnotation && hasIdAnnotation && hasPackage,
        issues: [],
        hasTableAnnotation,
        hasIdAnnotation,
        hasPackage
    };
};

/**
 * Extract entity information from a Java class
 * 
 * @param {string} classContent - Java class content
 * @returns {Object} Entity information
 */
exports.extractEntityInfo = function(classContent) {
    const tableMatch = classContent.match(/@Table\(name\s*=\s*"([^"]+)"\)/);
    const idMatch = classContent.match(/@Id\s+([^;]+);/);
    const classMatch = classContent.match(/public\s+class\s+([^\s{]+)/);
    const packageMatch = classContent.match(/package\s+([^;]+);/);
    
    return {
        tableName: tableMatch ? tableMatch[1] : null,
        idField: idMatch ? idMatch[1].trim() : null,
        className: classMatch ? classMatch[1] : null,
        packageName: packageMatch ? packageMatch[1] : null,
        fields: extractFields(classContent)
    };
};

/**
 * Extract fields from a Java class
 * 
 * @param {string} classContent - Java class content
 * @returns {Array} Array of field information
 */
function extractFields(classContent) {
    const fieldRegex = /(?:@\w+\s+)*private\s+([^\s]+)\s+([^;]+);/g;
    const fields = [];
    let match;
    
    while ((match = fieldRegex.exec(classContent)) !== null) {
        fields.push({
            type: match[1],
            name: match[2].trim(),
            annotations: extractAnnotations(classContent, match[2])
        });
    }
    
    return fields;
}

/**
 * Extract annotations for a specific field
 * 
 * @param {string} classContent - Java class content
 * @param {string} fieldName - Field name
 * @returns {Array} Array of annotations
 */
function extractAnnotations(classContent, fieldName) {
    const fieldPattern = new RegExp(`(?:@\\w+\\s+[^;]+;\\s*)*private\\s+[^\\s]+\\s+${fieldName};`);
    const match = classContent.match(fieldPattern);
    
    if (!match) return [];
    
    const fieldText = match[0];
    const annotationRegex = /@(\w+)(?:\s*\(([^)]*)\))?/g;
    const annotations = [];
    let annotationMatch;
    
    while ((annotationMatch = annotationRegex.exec(fieldText)) !== null) {
        annotations.push({
            name: annotationMatch[1],
            parameters: annotationMatch[2] || ''
        });
    }
    
    return annotations;
}

/**
 * Generate a basic norm4j configuration
 * 
 * @param {Object} params - Configuration parameters
 * @param {string} params.package - Base package name
 * @param {string} params.dialect - Database dialect
 * @returns {string} Configuration content
 */
exports.generateConfig = function(params) {
    const { package: pkg, dialect } = params;
    
    return `// norm4j Configuration
// Database: ${dialect}
// Package: ${pkg}

import javax.sql.DataSource;
import org.norm4j.TableManager;
import org.norm4j.metadata.MetadataManager;

public class Norm4jConfig {
    private final DataSource dataSource;
    private final TableManager tableManager;

    public Norm4jConfig(DataSource dataSource) {
        this.dataSource = dataSource;
        this.tableManager = createTableManager();
    }

    private TableManager createTableManager() {
        MetadataManager metadataManager = new MetadataManager();
        
        // Register all entities
        metadataManager.registerPackage("${pkg}");
        
        // Create tables
        metadataManager.createTables(dataSource);
        
        return new TableManager(dataSource, metadataManager);
    }

    public TableManager getTableManager() {
        return tableManager;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
`;
};

/**
 * Generate a test base class
 * 
 * @param {Object} params - Test parameters
 * @param {string} params.package - Test package
 * @param {string} params.dialect - Database dialect
 * @returns {string} Test base class content
 */
exports.generateTestBase = function(params) {
    const { package: pkg, dialect } = params;
    
    return `package ${pkg};

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.norm4j.TableManager;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.dialects.${dialect.charAt(0).toUpperCase() + dialect.slice(1)}Dialect;

public abstract class BaseTest {
    private static DataSource dataSource;
    private static TableManager tableManager;

    @BeforeAll
    public static void setup() {
        // Initialize data source
        dataSource = createTestDataSource();
        
        // Initialize norm4j
        MetadataManager metadataManager = new MetadataManager();
        metadataManager.registerPackage("${pkg}.entities");
        metadataManager.createTables(dataSource);
        
        tableManager = new TableManager(dataSource, metadataManager);
    }

    @AfterAll
    public static void cleanup() {
        // Cleanup resources
        if (tableManager != null) {
            // Add cleanup logic here
        }
    }

    protected static DataSource getDataSource() {
        return dataSource;
    }

    protected static TableManager getTableManager() {
        return tableManager;
    }

    protected static DataSource createTestDataSource() {
        // Implement test data source creation
        // This would typically use HikariCP or similar
        return null;
    }

    protected void dropTable(String schema, String tableName) {
        String sql = String.format("DROP TABLE IF EXISTS %s.%s CASCADE", schema, tableName);
        getTableManager().execute(sql);
    }
}
`;
};

/**
 * Generate a Maven plugin configuration
 * 
 * @param {Object} params - Plugin parameters
 * @param {string} params.package - Package to scan
 * @param {Array} params.dialects - Dialects to generate
 * @param {string} params.version - Schema version
 * @returns {string} Maven plugin configuration
 */
exports.generateMavenPluginConfig = function(params) {
    const { package: pkg, dialects, version } = params;
    
    const dialectElements = dialects.map(dialect => `        <dialect>${dialect}</dialect>`).join('\n');
    
    return `<plugin>
    <groupId>org.norm4j</groupId>
    <artifactId>norm4j-maven-plugin</artifactId>
    <version>1.1.25</version>
    <configuration>
        <packages>
            <package>${pkg}</package>
        </packages>
        <dialects>
${dialectElements}
        </dialects>
        <version>${version}</version>
        <outputDirectory>${project.basedir}/src/main/resources/db</outputDirectory>
    </configuration>
    <executions>
        <execution>
            <id>generate-ddl</id>
            <phase>process-classes</phase>
            <goals>
                <goal>generate-ddl</goal>
            </goals>
        </execution>
    </executions>
</plugin>`;
};

/**
 * Generate a schema synchronizer configuration
 * 
 * @param {Object} params - Synchronizer parameters
 * @param {Array} params.versions - Version configurations
 * @returns {string} Schema synchronizer code
 */
exports.generateSchemaSynchronizer = function(params) {
    const { versions } = params;
    
    const versionElements = versions.map(version => `
    .version("${version.name}")
        .description("${version.description}")
        ${version.schema ? `.schema()
            .enableAutoCreation(${version.schema.enableAutoCreation || 'false'})
            .enableAutoMigration(${version.schema.enableAutoMigration || 'false'})
        .endSchema()` : ''}
        ${version.statements ? version.statements.map(stmt => 
            stmt.dialect ? 
                `.execute("${stmt.sql}", ${stmt.dialect}Dialect.class)` : 
                `.execute("${stmt.sql}")`
        ).join('\n        ') : ''}
    .endVersion()`).join('');
    
    return `new SchemaSynchronizer(tableManager)
${versionElements}
    .apply();`;
};

/**
 * Generate a DTO class from an entity
 * 
 * @param {Object} params - DTO parameters
 * @param {string} params.entityName - Entity class name
 * @param {string} params.package - Target package
 * @param {Array} params.fields - Fields to include
 * @returns {string} DTO class content
 */
exports.generateDTO = function(params) {
    const { entityName, package: pkg, fields } = params;
    const dtoName = entityName + 'DTO';
    
    const fieldDeclarations = fields.map(field => 
        `    private ${field.type} ${field.name};`
    ).join('\n');
    
    const getterSetters = fields.flatMap(field => [
        `    public ${field.type} get${field.name.charAt(0).toUpperCase() + field.name.slice(1)}() {`,
        `        return ${field.name};`,
        `    }`,
        `    public void set${field.name.charAt(0).toUpperCase() + field.name.slice(1)}(${field.type} ${field.name}) {`,
        `        this.${field.name} = ${field.name};`,
        `    }`
    ]).join('\n');
    
    return `package ${pkg};

public class ${dtoName} {
${fieldDeclarations}

${getterSetters}
}
`;
};

/**
 * Generate a RecordMapper configuration
 * 
 * @param {Object} params - Mapper parameters
 * @param {string} params.entity - Entity class name
 * @param {string} params.dto - DTO class name
 * @param {Array} params.mappings - Field mappings
 * @param {Array} params.joins - Join configurations
 * @returns {string} RecordMapper code
 */
exports.generateRecordMapper = function(params) {
    const { entity, dto, mappings, joins } = params;
    
    const mappingElements = mappings.map(mapping => 
        `.map(${entity}::get${mapping.source.charAt(0).toUpperCase() + mapping.source.slice(1)})` +
        `.to(${dto}::set${mapping.target.charAt(0).toUpperCase() + mapping.target.slice(1)})`
    ).join('\n');
    
    const joinElements = joins.map(join => 
        `.join(${dto}::get${join.target.charAt(0).toUpperCase() + join.target.slice(1)}, ${join.entity}, ${join.dto})`
    ).join('\n');
    
    return `RecordMapper<${entity}, ${dto}> mapper = RecordMapperBuilder.from(${entity}.class, ${dto}.class)
${mappingElements}
${joinElements}
    .endJoin()
    .build(tableManager);

${dto} dto = mapper.map(entityInstance);
`;
};

/**
 * Analyze a norm4j query
 * 
 * @param {string} query - norm4j query code
 * @returns {Object} Query analysis
 */
exports.analyzeQuery = function(query) {
    const selectMatch = query.match(/\.select\(([^)]+)\)/);
    const fromMatch = query.match(/\.from\(([^)]+)\)/);
    const whereMatch = query.match(/\.where\(([^)]+)\)/);
    const joinMatch = query.match(/\.join\(([^)]+)\)/);
    
    return {
        select: selectMatch ? selectMatch[1] : null,
        from: fromMatch ? fromMatch[1] : null,
        where: whereMatch ? whereMatch[1] : null,
        joins: joinMatch ? joinMatch[1] : null,
        isSelect: query.includes('.select('),
        isUpdate: query.includes('.update('),
        isDelete: query.includes('.delete('),
        hasWhere: whereMatch !== null,
        hasJoins: joinMatch !== null
    };
};

/**
 * Generate a basic norm4j project structure
 * 
 * @param {Object} params - Project parameters
 * @param {string} params.baseDir - Base directory
 * @param {string} params.package - Base package
 * @returns {Object} Project structure information
 */
exports.generateProjectStructure = function(params) {
    const { baseDir, package: pkg } = params;
    const packagePath = pkg.replace('.', '/');
    
    return {
        structure: {
            baseDir,
            src: {
                main: {
                    java: {
                        [packagePath]: {
                            entities: 'Entity classes',
                            dtos: 'DTO classes',
                            mappers: 'RecordMapper configurations',
                            services: 'Service classes',
                            config: 'Configuration classes'
                        }
                    },
                    resources: {
                        db: 'Database resources',
                        config: 'Configuration files'
                    }
                },
                test: {
                    java: {
                        [packagePath]: {
                            tests: 'Test classes',
                            fixtures: 'Test fixtures'
                        }
                    },
                    resources: {
                        config: 'Test configuration'
                    }
                }
            },
            pom: 'Maven configuration'
        },
        files: [
            'src/main/java/com/example/entities/Customer.java',
            'src/main/java/com/example/dtos/CustomerDTO.java',
            'src/main/java/com/example/mappers/CustomerMapper.java',
            'src/main/java/com/example/services/CustomerService.java',
            'src/main/java/com/example/config/Norm4jConfig.java',
            'src/main/resources/db/v0.1/schema.json',
            'src/main/resources/db/v0.1/postgresql/ddl.sql',
            'src/test/java/com/example/tests/CustomerTest.java',
            'src/test/java/com/example/fixtures/TestData.java',
            'pom.xml'
        ]
    };
};

/**
 * Validate a norm4j query
 * 
 * @param {string} query - norm4j query code
 * @returns {Object} Validation result
 */
exports.validateQuery = function(query) {
    const hasSelect = query.includes('.select(');
    const hasFrom = query.includes('.from(');
    const hasGetResult = query.includes('.getResultList(') || query.includes('.getSingleResult(');
    
    return {
        valid: hasSelect && hasFrom && hasGetResult,
        issues: [],
        hasSelect,
        hasFrom,
        hasGetResult,
        suggestions: []
    };
};

/**
 * Generate database-specific configuration
 * 
 * @param {Object} params - Configuration parameters
 * @param {string} params.dialect - Database dialect
 * @param {string} params.url - JDBC URL
 * @param {string} params.username - Database username
 * @param {string} params.password - Database password
 * @returns {string} Database configuration
 */
exports.generateDatabaseConfig = function(params) {
    const { dialect, url, username, password } = params;
    
    return `// ${dialect} Database Configuration

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConfig {
    public static DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl("${url}");
        config.setUsername("${username}");
        config.setPassword("${password}");
        
        // Connection pool settings
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // ${dialect}-specific settings
        ${getDialectSpecificSettings(dialect)}
        
        return new HikariDataSource(config);
    }
    
    private static String getDialectSpecificSettings(String dialect) {
        switch (dialect.toLowerCase()) {
            case "postgresql":
                return "config.addDataSourceProperty('cachePrepStmts', 'true');\n" +
                       "config.addDataSourceProperty('prepStmtCacheSize', '250');\n" +
                       "config.addDataSourceProperty('prepStmtCacheSqlLimit', '2048');";
            case "mariadb":
                return "config.addDataSourceProperty('useServerPrepStmts', 'true');\n" +
                       "config.addDataSourceProperty('cachePrepStmts', 'true');\n" +
                       "config.addDataSourceProperty('prepStmtCacheSize', '250');";
            case "sqlserver":
                return "config.addDataSourceProperty('encrypt', 'false');\n" +
                       "config.addDataSourceProperty('trustServerCertificate', 'false');";
            case "oracle":
                return "config.addDataSourceProperty('oracle.jdbc.J2EE13Compliant', 'true');";
            default:
                return "// No dialect-specific settings";
        }
    }
}
`;
};

function getDialectSpecificSettings(dialect) {
    switch (dialect.toLowerCase()) {
        case 'postgresql':
            return 'config.addDataSourceProperty("cachePrepStmts", "true");\n' +
                   'config.addDataSourceProperty("prepStmtCacheSize", "250");\n' +
                   'config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");';
        case 'mariadb':
            return 'config.addDataSourceProperty("useServerPrepStmts", "true");\n' +
                   'config.addDataSourceProperty("cachePrepStmts", "true");\n' +
                   'config.addDataSourceProperty("prepStmtCacheSize", "250");';
        case 'sqlserver':
            return 'config.addDataSourceProperty("encrypt", "false");\n' +
                   'config.addDataSourceProperty("trustServerCertificate", "false");';
        case 'oracle':
            return 'config.addDataSourceProperty("oracle.jdbc.J2EE13Compliant", "true");';
        default:
            return '// No dialect-specific settings';
    }
}

module.exports = {
    isValidEntity: exports.isValidEntity,
    extractEntityInfo: exports.extractEntityInfo,
    generateConfig: exports.generateConfig,
    generateTestBase: exports.generateTestBase,
    generateMavenPluginConfig: exports.generateMavenPluginConfig,
    generateSchemaSynchronizer: exports.generateSchemaSynchronizer,
    generateDTO: exports.generateDTO,
    generateRecordMapper: exports.generateRecordMapper,
    analyzeQuery: exports.analyzeQuery,
    generateProjectStructure: exports.generateProjectStructure,
    validateQuery: exports.validateQuery,
    generateDatabaseConfig: exports.generateDatabaseConfig
};
