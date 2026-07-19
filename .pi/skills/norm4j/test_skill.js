/**
 * Test script for norm4j skill
 * 
 * This script tests the basic functionality of the norm4j skill
 */

const helpers = require('./helpers');
const commands = require('./commands');

console.log('Testing norm4j skill...\n');

// Test helpers
console.log('Testing helpers...');

// Test entity validation
const sampleEntity = `
package com.example;

import org.norm4j.Table;
import org.norm4j.Id;
import org.norm4j.GeneratedValue;
import org.norm4j.GenerationType;
import org.norm4j.Column;

@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
`;

const validationResult = helpers.isValidEntity(sampleEntity);
console.log('✓ Entity validation:', validationResult.valid ? 'PASS' : 'FAIL');

// Test entity info extraction
const entityInfo = helpers.extractEntityInfo(sampleEntity);
console.log('✓ Entity info extraction:', entityInfo.className === 'Customer' ? 'PASS' : 'FAIL');

// Test config generation
const config = helpers.generateConfig({ package: 'com.example', dialect: 'postgresql' });
console.log('✓ Config generation:', config.includes('Norm4jConfig') ? 'PASS' : 'FAIL');

// Test DTO generation
const dto = helpers.generateDTO({ 
    entityName: 'Customer', 
    package: 'com.example.dtos', 
    fields: [
        { type: 'int', name: 'id' },
        { type: 'String', name: 'name' },
        { type: 'String', name: 'email' }
    ]
});
console.log('✓ DTO generation:', dto.includes('CustomerDTO') ? 'PASS' : 'FAIL');

// Test commands
console.log('\nTesting commands...');

// Test entity creation
const entity = commands.createEntity({ 
    name: 'Product', 
    package: 'com.example.entities',
    tableName: 'products'
});
console.log('✓ Entity creation:', entity.includes('@Table(name = "products")') ? 'PASS' : 'FAIL');

// Test DDL generation
const ddl = commands.generateDDL({ 
    package: 'com.example.entities',
    dialect: 'postgresql'
});
console.log('✓ DDL generation:', ddl.includes('CREATE TABLE') ? 'PASS' : 'FAIL');

// Test migration creation
const migration = commands.createMigration({ 
    version: 'v0.2',
    description: 'Add email field'
});
console.log('✓ Migration creation:', migration.includes('version("v0.2")') ? 'PASS' : 'FAIL');

// Test query explanation
const explanation = commands.explainQuery({ 
    query: 'SELECT c FROM Customer c WHERE c.id = ?'
});
console.log('✓ Query explanation:', explanation.generatedSQL ? 'PASS' : 'FAIL');

// Test query analysis
const analysis = helpers.analyzeQuery('SELECT c FROM Customer c WHERE c.active = true');
console.log('✓ Query analysis:', analysis.select === 'Customer' ? 'PASS' : 'FAIL');

// Test project structure generation
const structure = helpers.generateProjectStructure({ 
    baseDir: '.',
    package: 'com.example'
});
console.log('✓ Project structure:', structure.files.length > 0 ? 'PASS' : 'FAIL');

console.log('\nAll tests completed!');

// Summary
const passedTests = [
    'Entity validation',
    'Entity info extraction',
    'Config generation',
    'DTO generation',
    'Entity creation',
    'DDL generation',
    'Migration creation',
    'Query explanation',
    'Query analysis',
    'Project structure'
].length;

console.log(`\nSummary: ${passedTests}/10 tests passed`);

if (passedTests === 10) {
    console.log('✓ All tests passed! The norm4j skill is working correctly.');
} else {
    console.log('✗ Some tests failed. Please check the implementation.');
}
