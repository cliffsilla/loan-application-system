# Git Instructions for Loan Application System

## Initial Setup

### 1. Initialize Git Repository (if not already done)
```bash
git init
```

### 2. Configure Git User Information
```bash
git config user.name "Your Name"
git config user.email "your.email@example.com"
```

## Working with .gitignore

The project already includes a comprehensive .gitignore file that excludes:
- Compiled class files (*.class)
- Log files (*.log)
- Package files (*.jar, *.war, etc.)
- IDE-specific files (.idea/, .vscode/, etc.)
- Build directories (target/, build/)
- Most markdown files except README.md and those in specified documentation directories

### Checking Ignored Files
To verify which files are being ignored:
```bash
git status --ignored
```

## Adding and Committing Files

### 1. Stage All Files
```bash
git add .
```

### 2. Commit Changes
```bash
git commit -m "Initial commit with complete loan application system"
```

For more specific commits, use descriptive messages:
```bash
git commit -m "Add markdown documentation and update README"
```

## Connecting to a Remote Repository

### 1. Add a Remote Repository
```bash
git remote add origin https://github.com/yourusername/loan-application-system.git
```
Replace the URL with your actual repository URL.

### 2. Push to Remote Repository
```bash
git push -u origin main
```
If you're using a different branch name (like "master"), replace "main" with your branch name.

## Working with Branches

### 1. Create a New Branch
```bash
git checkout -b feature/new-feature
```

### 2. Switch Between Branches
```bash
git checkout main
```

### 3. Merge Branches
```bash
git checkout main
git merge feature/new-feature
```

## Best Practices

1. **Commit Frequently**: Make small, focused commits with clear messages
2. **Pull Before Push**: Always pull the latest changes before pushing
   ```bash
   git pull origin main
   ```
3. **Use Branches**: Create separate branches for new features or bug fixes
4. **Review Changes**: Before committing, review your changes
   ```bash
   git diff
   ```
5. **Protect Sensitive Data**: Never commit sensitive information like API keys or passwords

## Handling Large Files

If you need to work with large files that shouldn't be in Git:

1. Add them to .gitignore
2. Consider using Git LFS (Large File Storage) for binary files

## Troubleshooting

### Fixing a Bad Commit
```bash
git commit --amend
```

### Reverting a Commit
```bash
git revert <commit-hash>
```

### Unstaging Files
```bash
git reset HEAD <file>
```
