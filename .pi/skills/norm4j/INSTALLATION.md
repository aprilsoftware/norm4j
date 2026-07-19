# norm4j Skill Installation Guide

## Overview

This guide explains how to install and use the norm4j skill in the Pi coding agent environment.

## Installation

The norm4j skill is already installed in the correct location:

```
.pi/agent/skills/norm4j/
```

### Required Files

The skill includes all necessary components:

```
📁 norm4j skill directory:
├── skill.json                  # Skill manifest and configuration
├── commands.js                 # Command implementations
├── helpers.js                  # Utility functions
├── SKILL.md                    # Comprehensive documentation
├── examples.md                 # Practical code examples
├── README.md                   # User guide
├── demo.md                     # Usage demonstrations
├── test_skill.js               # Automated testing
├── verify_skill.js             # Installation verification
└── SUMMARY.md                  # Technical summary
```

## Verification

To verify the skill is properly installed and functional:

```bash
cd .pi/agent/skills/norm4j
node verify_skill.js
```

This will output a verification report showing:
- ✅ skill.json configuration
- ✅ Required files presence and sizes
- ✅ Command functionality
- ✅ Helper functionality
- ✅ Skill summary

## Usage

### Basic Commands

The skill provides 13 commands for working with norm4j:

```bash
# Create a new entity
norm4j:create-entity --name Customer --package com.example.entities

# Generate DDL for a schema
norm4j:generate-ddl --package com.example.entities --dialect postgresql

# Create a new migration
norm4j:create-migration --version v0.1 --description "Initial schema"

# Explain a norm4j query
norm4j:explain-query --query "SELECT c FROM Customer c WHERE c.id = ?"

# Validate an entity
norm4j:validate-entity --file src/main/java/com/example/Customer.java

# Generate DTO
norm4j:generate-dto --entity Customer --package com.example.dtos

# Generate mapper
norm4j:generate-mapper --entity Customer --dto CustomerDTO

# Generate configuration
norm4j:generate-config --package com.example --dialect postgresql

# Generate test base class
norm4j:generate-test-base --package com.example.tests --dialect postgresql

# Generate project structure
norm4j:generate-project-structure --baseDir . --package com.example

# Generate database configuration
norm4j:generate-database-config --dialect postgresql --url "jdbc:postgresql://localhost:5432/mydb" --username user --password password

# Generate Maven plugin configuration
norm4j:generate-maven-plugin-config --package com.example.entities --dialects postgresql,mariadb --version v0.1

# Validate a query
norm4j:validate-query --query "SELECT c FROM Customer c WHERE c.id = ?"
```

### Command Options

Each command supports various options. Use `--help` to see available options:

```bash
norm4j:create-entity --help
```

## Documentation

### Quick Start

For immediate help, see:

- **README.md**: Complete user guide with all commands
- **demo.md**: Practical usage demonstrations
- **SKILL.md**: Comprehensive norm4j documentation

### Reference Materials

- **examples.md**: Practical code examples for all scenarios
- **SKILL.md**: In-depth norm4j documentation
- **SUMMARY.md**: Technical summary of the skill

## Testing

### Automated Tests

Run the automated test suite:

```bash
cd .pi/agent/skills/norm4j
node test_skill.js
```

This tests all major functionality:
- Entity validation
- Entity information extraction
- Configuration generation
- DTO generation
- Entity creation commands
- DDL generation
- Migration creation
- Query explanation
- Query analysis
- Project structure generation

### Manual Testing

Test individual commands:

```bash
# Test entity creation
node -e "const cmd = require('./commands.js'); console.log(cmd.createEntity({name: 'Test', package: 'com.test'}))"

# Test helper functions
node -e "const hlp = require('./helpers.js'); console.log(hlp.isValidEntity('package com.test;\n@Table(name=\"test\")\npublic class Test { @Id private int id; }'))"
```

## Integration with Pi Agent

The skill is automatically available in the Pi agent environment. You can use it by:

1. **Direct commands**: Use the command syntax shown above
2. **Interactive help**: Ask for help on specific commands
3. **Documentation access**: Read the included documentation files

## Troubleshooting

### Common Issues

**Issue**: Skill commands not found
- **Solution**: Ensure you're in the correct directory or the skill path is in your Node.js path
- **Check**: `cd .pi/agent/skills/norm4j && node verify_skill.js`

**Issue**: Missing dependencies
- **Solution**: Ensure Node.js is installed (version 14+)
- **Check**: `node --version`

**Issue**: Command not working as expected
- **Solution**: Check the command options with `--help`
- **Check**: Review the examples in examples.md

**Issue**: File generation issues
- **Solution**: Ensure you have write permissions in the target directory
- **Check**: Try generating to a different location

### Debugging

Enable debug output:

```bash
NODE_DEBUG=skill node your_command.js
```

## Best Practices

### Skill Usage

1. **Start with examples**: Review examples.md for common patterns
2. **Use validation**: Always validate entities and queries
3. **Check documentation**: Consult SKILL.md for detailed information
4. **Test thoroughly**: Use the test suite to verify functionality
5. **Review generated code**: Always review auto-generated code

### norm4j Development

1. **Follow conventions**: Use consistent naming and patterns
2. **Version migrations**: Always use SchemaSynchronizer
3. **Test migrations**: Test migrations before applying to production
4. **Use explicit joins**: Don't rely on implicit loading
5. **Optimize queries**: Use pagination and selective loading

## Support

### Skill Support

For issues with the skill itself:

1. Check the verification script output
2. Review the test results
3. Consult the documentation files
4. Check for missing dependencies

### norm4j Support

For issues with norm4j itself:

1. Consult the norm4j documentation
2. Review the examples in the norm4j repository
3. Check the Medium articles linked in SKILL.md
4. Consider opening an issue in the norm4j repository

## Resources

### Skill Resources

- **Verification**: `verify_skill.js` - Installation verification
- **Testing**: `test_skill.js` - Automated test suite
- **Documentation**: `SKILL.md`, `README.md`, `examples.md`, `demo.md`
- **Reference**: `SUMMARY.md` - Technical overview

### norm4j Resources

- **Documentation**: [norm4j README](https://github.com/aprilsoftware/norm4j/README.md)
- **Articles**: [Medium articles](https://medium.com/@cedric.nanni)
- **Source**: [GitHub repository](https://github.com/aprilsoftware/norm4j)
- **Tests**: [norm4j-test module](https://github.com/aprilsoftware/norm4j/tree/main/norm4j-test)

## Updating the Skill

To update the skill:

1. **Backup**: Create a backup of your current skill directory
2. **Update files**: Replace files with updated versions
3. **Verify**: Run `verify_skill.js` to ensure everything works
4. **Test**: Run `test_skill.js` to verify functionality
5. **Document**: Update documentation as needed

## Uninstallation

To remove the skill:

```bash
rm -rf .pi/agent/skills/norm4j
```

## Summary

The norm4j skill is now properly installed and ready to use. It provides:

- ✅ 13 powerful commands for norm4j development
- ✅ Comprehensive documentation and examples
- ✅ Automated testing and verification
- ✅ Integration with Pi coding agent
- ✅ Support for multiple database dialects
- ✅ Best practices and patterns

Start using the skill by running the verification script and exploring the documentation!
