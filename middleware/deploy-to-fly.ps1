# Deployment script for middleware to fly.io

# Configuration
$APP_NAME = "middleware-credible-assessment"
$PROJECT_DIR = $PSScriptRoot
$JAVA_VERSION = "17"

Write-Host "\n=============================================" -ForegroundColor Cyan
Write-Host "MIDDLEWARE DEPLOYMENT TO FLY.IO" -ForegroundColor Cyan
Write-Host "=============================================\n" -ForegroundColor Cyan

Write-Host "Starting deployment process for $APP_NAME" -ForegroundColor Yellow
Write-Host "Project directory: $PROJECT_DIR" -ForegroundColor Yellow

# Step 1: Verify prerequisites
Write-Host "\n=== Step 1: Verifying prerequisites ===" -ForegroundColor Green

# Check if flyctl is installed
Write-Host "Checking if flyctl is installed..." -ForegroundColor Gray
try {
    $flyVersion = & flyctl version
    Write-Host "flyctl is installed: $flyVersion" -ForegroundColor Green
} catch {
    Write-Host "Error: flyctl is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install flyctl: https://fly.io/docs/hands-on/install-flyctl/" -ForegroundColor Red
    exit 1
}

# Check if Java is installed
Write-Host "Checking if Java is installed..." -ForegroundColor Gray
try {
    $javaVersion = & java -version 2>&1
    Write-Host "Java is installed: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "Error: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java $JAVA_VERSION or higher" -ForegroundColor Red
    exit 1
}

# Check if Maven is installed
Write-Host "Checking if Maven is installed..." -ForegroundColor Gray
try {
    $mvnVersion = & mvn --version
    Write-Host "Maven is installed: $($mvnVersion -split "`n" | Select-Object -First 1)" -ForegroundColor Green
} catch {
    Write-Host "Error: Maven is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Maven" -ForegroundColor Red
    exit 1
}

# Step 2: Build the application
Write-Host "\n=== Step 2: Building the application ===" -ForegroundColor Green
Write-Host "Running Maven clean package..." -ForegroundColor Gray

Set-Location -Path $PROJECT_DIR

try {
    & mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Maven build failed" -ForegroundColor Red
        exit 1
    }
    Write-Host "Maven build successful" -ForegroundColor Green
} catch {
    Write-Host "Error during Maven build: $_" -ForegroundColor Red
    exit 1
}

# Step 3: Verify the JAR file was created
Write-Host "\n=== Step 3: Verifying build artifacts ===" -ForegroundColor Green
$jarFile = Get-ChildItem -Path "$PROJECT_DIR/target" -Filter "*.jar" | Where-Object { -not $_.Name.Contains('sources') -and -not $_.Name.Contains('javadoc') }

if (-not $jarFile) {
    Write-Host "Error: JAR file not found in target directory" -ForegroundColor Red
    exit 1
}

Write-Host "Found JAR file: $($jarFile.Name)" -ForegroundColor Green
Write-Host "JAR file size: $([math]::Round($jarFile.Length / 1MB, 2)) MB" -ForegroundColor Green

# Step 4: Verify fly.toml exists
Write-Host "\n=== Step 4: Verifying fly.toml configuration ===" -ForegroundColor Green
$flyTomlPath = "$PROJECT_DIR/fly.toml"

if (-not (Test-Path $flyTomlPath)) {
    Write-Host "Error: fly.toml not found at $flyTomlPath" -ForegroundColor Red
    exit 1
}

Write-Host "Found fly.toml configuration file" -ForegroundColor Green

# Step 5: Check if logged in to fly.io
Write-Host "\n=== Step 5: Checking fly.io authentication ===" -ForegroundColor Green

try {
    $whoami = & flyctl auth whoami
    Write-Host "Logged in to fly.io as: $whoami" -ForegroundColor Green
} catch {
    Write-Host "Not logged in to fly.io. Please log in." -ForegroundColor Yellow
    & flyctl auth login
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Failed to log in to fly.io" -ForegroundColor Red
        exit 1
    }
}

# Step 6: Deploy to fly.io
Write-Host "\n=== Step 6: Deploying to fly.io ===" -ForegroundColor Green
Write-Host "Deploying application to fly.io..." -ForegroundColor Gray

try {
    & flyctl deploy --remote-only
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Deployment to fly.io failed" -ForegroundColor Red
        exit 1
    }
    Write-Host "Deployment to fly.io successful" -ForegroundColor Green
} catch {
    Write-Host "Error during deployment: $_" -ForegroundColor Red
    exit 1
}

# Step 7: Verify deployment
Write-Host "\n=== Step 7: Verifying deployment ===" -ForegroundColor Green
Write-Host "Checking application status..." -ForegroundColor Gray

try {
    & flyctl status
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Warning: Could not check application status" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Warning: Could not check application status: $_" -ForegroundColor Yellow
}

# Step 8: Display application URL
Write-Host "\n=== Step 8: Application information ===" -ForegroundColor Green

$appUrl = "https://$APP_NAME.fly.dev"
Write-Host "Application URL: $appUrl" -ForegroundColor Green
Write-Host "Health check URL: $appUrl/health" -ForegroundColor Green
Write-Host "Scoring engine test URL: $appUrl/scoring-engine/test-connection" -ForegroundColor Green

Write-Host "\n=============================================" -ForegroundColor Cyan
Write-Host "DEPLOYMENT COMPLETED" -ForegroundColor Cyan
Write-Host "=============================================\n" -ForegroundColor Cyan

Write-Host "To test the scoring engine connection, run:" -ForegroundColor Yellow
Write-Host "Invoke-RestMethod -Uri '$appUrl/health'" -ForegroundColor Yellow
Write-Host "Invoke-RestMethod -Uri '$appUrl/scoring-engine/test-connection'" -ForegroundColor Yellow

Write-Host "\nTo view logs:" -ForegroundColor Yellow
Write-Host "flyctl logs" -ForegroundColor Yellow
