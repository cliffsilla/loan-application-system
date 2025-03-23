# Scoring Engine Connection Test Script
# This script directly tests the connection to the scoring engine from the middleware

# Configuration
$MIDDLEWARE_BASE_URL = "https://middleware-credible-assessment.fly.dev"
$LOCAL_MIDDLEWARE_URL = "http://localhost:8080"
$API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind0YWNtc2RpeXRxa2djd3lybWhxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI3MDgzNzksImV4cCI6MjA1ODI4NDM3OX0.axPG6US-rZnOKEPu1gAbNhUbk09O0F7y-ZOFWjC-CoE"

# Test customer IDs from memory
$CUSTOMER_IDS = @(
    "234774784",
    "318411216",
    "340397370",
    "366585630",
    "397178638"
)

# Retry settings
$MAX_RETRIES = 5
$RETRY_DELAY_MS = 2000

Write-Host "\n=============================================" -ForegroundColor Cyan
Write-Host "SCORING ENGINE CONNECTION TEST SCRIPT" -ForegroundColor Cyan
Write-Host "=============================================\n" -ForegroundColor Cyan

# Helper function to retry API calls
function Invoke-ApiWithRetry {
    param (
        [string]$Uri,
        [string]$Method = "Get",
        [object]$Body = $null,
        [int]$MaxRetries = $MAX_RETRIES,
        [int]$DelayMs = $RETRY_DELAY_MS,
        [string]$ErrorMessage = "API call failed"
    )

    $headers = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $API_KEY"
    }

    $params = @{
        Uri = $Uri
        Method = $Method
        Headers = $headers
        ContentType = "application/json"
    }

    if ($Body) {
        $params.Body = $Body
    }

    $attempt = 0
    $success = $false
    $result = $null

    while (-not $success -and $attempt -lt $MaxRetries) {
        $attempt++
        try {
            $result = Invoke-RestMethod @params
            $success = $true
        } catch {
            $statusCode = $_.Exception.Response.StatusCode
            
            if ($attempt -lt $MaxRetries) {
                Write-Host "Attempt $attempt failed: $($_.Exception.Message). Retrying in $(($DelayMs/1000)) seconds..." -ForegroundColor Yellow
                Start-Sleep -Milliseconds $DelayMs
                # Increase delay with each retry (exponential backoff)
                $DelayMs = $DelayMs * 1.5
            } else {
                Write-Host "$ErrorMessage after $MaxRetries attempts: $($_.Exception.Message)" -ForegroundColor Red
                Write-Host "Status Code: $statusCode" -ForegroundColor Red
                throw
            }
        }
    }

    return $result
}

# Function to test middleware health
function Test-MiddlewareHealth {
    param (
        [string]$BaseUrl
    )
    
    Write-Host "\n=== Testing Middleware Health ===" -ForegroundColor Green
    Write-Host "Request: GET $BaseUrl/health" -ForegroundColor Gray
    
    try {
        $healthResponse = Invoke-ApiWithRetry -Uri "$BaseUrl/health" -Method Get -ErrorMessage "Error checking middleware health"
        Write-Host "Middleware health status: $($healthResponse.status)" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "Middleware health check failed" -ForegroundColor Red
        return $false
    }
}

# Function to test scoring engine connection via middleware
function Test-ScoringEngineConnection {
    param (
        [string]$BaseUrl,
        [string]$CustomerNumber
    )
    
    Write-Host "\n=== Testing Scoring Engine Connection ===" -ForegroundColor Green
    Write-Host "Request: GET $BaseUrl/transactions/$CustomerNumber" -ForegroundColor Gray
    
    try {
        $transactionResponse = Invoke-ApiWithRetry -Uri "$BaseUrl/transactions/$CustomerNumber" -Method Get -ErrorMessage "Error retrieving transaction data"
        Write-Host "Successfully connected to scoring engine!" -ForegroundColor Green
        Write-Host "Retrieved $($transactionResponse.Count) transactions for customer $CustomerNumber" -ForegroundColor Green
        
        if ($transactionResponse -and $transactionResponse.Count -gt 0) {
            Write-Host "Sample transaction: $($transactionResponse[0] | ConvertTo-Json -Depth 1)" -ForegroundColor Gray
        }
        
        return $true
    } catch {
        Write-Host "Failed to connect to scoring engine" -ForegroundColor Red
        return $false
    }
}

# Function to test direct scoring engine connection via middleware
function Test-DirectScoringEngineConnection {
    param (
        [string]$BaseUrl
    )
    
    Write-Host "\n=== Testing Direct Scoring Engine Connection ===" -ForegroundColor Green
    Write-Host "Request: GET $BaseUrl/scoring-engine/test-connection" -ForegroundColor Gray
    
    try {
        $connectionResponse = Invoke-ApiWithRetry -Uri "$BaseUrl/scoring-engine/test-connection" -Method Get -ErrorMessage "Error testing direct scoring engine connection"
        Write-Host "Direct scoring engine connection test: $($connectionResponse.status)" -ForegroundColor Green
        Write-Host "Response: $($connectionResponse | ConvertTo-Json)" -ForegroundColor Gray
        return $true
    } catch {
        Write-Host "Direct scoring engine connection test failed" -ForegroundColor Red
        return $false
    }
}

# Main test sequence
$overallSuccess = $true

# First try the deployed middleware
Write-Host "Testing deployed middleware at $MIDDLEWARE_BASE_URL" -ForegroundColor Yellow

$middlewareHealthy = Test-MiddlewareHealth -BaseUrl $MIDDLEWARE_BASE_URL

if ($middlewareHealthy) {
    # Try each customer ID until one works
    $scoringEngineConnected = $false
    
    foreach ($customerId in $CUSTOMER_IDS) {
        Write-Host "\nTrying customer ID: $customerId" -ForegroundColor Yellow
        
        $result = Test-ScoringEngineConnection -BaseUrl $MIDDLEWARE_BASE_URL -CustomerNumber $customerId
        
        if ($result) {
            $scoringEngineConnected = $true
            break
        }
    }
    
    if (-not $scoringEngineConnected) {
        Write-Host "\nCould not connect to scoring engine with any customer ID" -ForegroundColor Red
        $overallSuccess = $false
        
        # Try direct connection test if available
        Test-DirectScoringEngineConnection -BaseUrl $MIDDLEWARE_BASE_URL
    }
} else {
    Write-Host "Deployed middleware is not healthy, skipping scoring engine tests" -ForegroundColor Red
    $overallSuccess = $false
}

# Try local middleware if it's running
Write-Host "\n\nWould you like to test local middleware at $LOCAL_MIDDLEWARE_URL? (y/n)" -ForegroundColor Yellow
$testLocal = Read-Host

if ($testLocal -eq "y") {
    Write-Host "\nTesting local middleware at $LOCAL_MIDDLEWARE_URL" -ForegroundColor Yellow
    
    $localMiddlewareHealthy = Test-MiddlewareHealth -BaseUrl $LOCAL_MIDDLEWARE_URL
    
    if ($localMiddlewareHealthy) {
        # Try each customer ID until one works
        $localScoringEngineConnected = $false
        
        foreach ($customerId in $CUSTOMER_IDS) {
            Write-Host "\nTrying customer ID: $customerId" -ForegroundColor Yellow
            
            $result = Test-ScoringEngineConnection -BaseUrl $LOCAL_MIDDLEWARE_URL -CustomerNumber $customerId
            
            if ($result) {
                $localScoringEngineConnected = $true
                break
            }
        }
        
        if (-not $localScoringEngineConnected) {
            Write-Host "\nCould not connect to scoring engine with any customer ID from local middleware" -ForegroundColor Red
            
            # Try direct connection test if available
            Test-DirectScoringEngineConnection -BaseUrl $LOCAL_MIDDLEWARE_URL
        }
    } else {
        Write-Host "Local middleware is not healthy or not running, skipping local scoring engine tests" -ForegroundColor Red
    }
}

Write-Host "\n=============================================" -ForegroundColor Cyan
Write-Host "SCORING ENGINE CONNECTION TEST COMPLETED" -ForegroundColor Cyan
if ($overallSuccess) {
    Write-Host "RESULT: SUCCESS - Scoring engine connection is working" -ForegroundColor Green
} else {
    Write-Host "RESULT: FAILED - Could not verify scoring engine connection" -ForegroundColor Red
    Write-Host "Please check the following:" -ForegroundColor Yellow
    Write-Host "1. Scoring engine URL is correct in application.properties" -ForegroundColor Yellow
    Write-Host "2. Scoring engine credentials are correct" -ForegroundColor Yellow
    Write-Host "3. Network connectivity to scoring engine is available" -ForegroundColor Yellow
    Write-Host "4. Timeout and retry settings are appropriate" -ForegroundColor Yellow
}
Write-Host "=============================================\n" -ForegroundColor Cyan
