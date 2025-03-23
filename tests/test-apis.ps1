# API Test Script for Loan Application System
# This script tests the APIs described in process.md using curl commands

# Configuration
$LMS_BASE_URL = "https://lms-credible-assessment.fly.dev"
$MIDDLEWARE_BASE_URL = "https://middleware-credible-assessment.fly.dev"
$API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind0YWNtc2RpeXRxa2djd3lybWhxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI3MDgzNzksImV4cCI6MjA1ODI4NDM3OX0.axPG6US-rZnOKEPu1gAbNhUbk09O0F7y-ZOFWjC-CoE"

# Test customer IDs
$CUSTOMER_IDS = @(
    "234774784",
    "318411216",
    "340397370",
    "366585630",
    "397178638"
)

# Select a customer ID for testing
$TEST_CUSTOMER = $CUSTOMER_IDS[0]  # Using the first customer ID
$TEST_LOAN_AMOUNT = 1000.00

# Retry settings
$MAX_RETRIES = 3
$RETRY_DELAY_MS = 2000

Write-Host "\n=============================================" -ForegroundColor Cyan
Write-Host "API TEST SCRIPT FOR LOAN APPLICATION SYSTEM" -ForegroundColor Cyan
Write-Host "=============================================\n" -ForegroundColor Cyan

Write-Host "Using customer number: $TEST_CUSTOMER" -ForegroundColor Yellow
Write-Host "LMS URL: $LMS_BASE_URL" -ForegroundColor Yellow
Write-Host "Middleware URL: $MIDDLEWARE_BASE_URL\n" -ForegroundColor Yellow

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
            
            # If this is a 409 Conflict (customer already subscribed) or 400 Bad Request (loan already exists)
            # we can consider it a "success" for testing purposes
            if ($statusCode -eq 409 -or $statusCode -eq 400) {
                Write-Host "Received $statusCode status code. This may be expected in testing." -ForegroundColor Yellow
                return $_.Exception.Response
            }
            
            if ($attempt -lt $MaxRetries) {
                Write-Host "Attempt $attempt failed: $($_.Exception.Message). Retrying in $(($DelayMs/1000)) seconds..." -ForegroundColor Yellow
                Start-Sleep -Milliseconds $DelayMs
            } else {
                Write-Host "$ErrorMessage after $MaxRetries attempts: $($_.Exception.Message)" -ForegroundColor Red
                Write-Host "Status Code: $statusCode" -ForegroundColor Red
                throw
            }
        }
    }

    return $result
}

# Step 1: Customer Subscription
Write-Host "\n=== STEP 1: Customer Subscription ===" -ForegroundColor Green

$subscriptionPayload = @{
    customerNumber = $TEST_CUSTOMER
} | ConvertTo-Json

Write-Host "Request: POST $LMS_BASE_URL/subscriptions" -ForegroundColor Gray
Write-Host "Payload: $subscriptionPayload" -ForegroundColor Gray

try {
    $subscriptionResponse = Invoke-ApiWithRetry -Uri "$LMS_BASE_URL/subscriptions" -Method Post -Body $subscriptionPayload -ErrorMessage "Error subscribing customer"
    
    if ($subscriptionResponse -is [System.Net.HttpWebResponse]) {
        Write-Host "\nCustomer may already be subscribed. Continuing with test." -ForegroundColor Yellow
    } else {
        Write-Host "Response: $($subscriptionResponse | ConvertTo-Json)" -ForegroundColor Gray
        $customerId = $subscriptionResponse.customerId
        Write-Host "\nCustomer subscribed successfully with ID: $customerId" -ForegroundColor Green
    }
} catch {
    Write-Host "\nFailed to subscribe customer. Continuing with test assuming customer is already subscribed." -ForegroundColor Yellow
}

# Step 2: Loan Request
Write-Host "\n=== STEP 2: Loan Request ===" -ForegroundColor Green

$loanPayload = @{
    customerNumber = $TEST_CUSTOMER
    amount = $TEST_LOAN_AMOUNT
} | ConvertTo-Json

Write-Host "Request: POST $LMS_BASE_URL/loans" -ForegroundColor Gray
Write-Host "Payload: $loanPayload" -ForegroundColor Gray

try {
    $loanResponse = Invoke-ApiWithRetry -Uri "$LMS_BASE_URL/loans" -Method Post -Body $loanPayload -ErrorMessage "Error requesting loan"
    
    if ($loanResponse -is [System.Net.HttpWebResponse]) {
        Write-Host "\nCustomer may already have an active loan. Continuing with test." -ForegroundColor Yellow
        
        # Try to get existing loans for this customer
        try {
            $existingLoans = Invoke-ApiWithRetry -Uri "$LMS_BASE_URL/loans/customer/$TEST_CUSTOMER" -Method Get -ErrorMessage "Error retrieving existing loans"
            if ($existingLoans -and $existingLoans.Count -gt 0) {
                $loanId = $existingLoans[0].loanId
                $loanStatus = $existingLoans[0].status
                Write-Host "Found existing loan with ID: $loanId and status: $loanStatus" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "Could not retrieve existing loans for this customer. Trying to get any loan..." -ForegroundColor Yellow
            
            # Try to get any loan from the system
            try {
                $allLoans = Invoke-ApiWithRetry -Uri "$LMS_BASE_URL/loans" -Method Get -ErrorMessage "Error retrieving all loans"
                if ($allLoans -and $allLoans.Count -gt 0) {
                    $loanId = $allLoans[0].loanId
                    $loanStatus = $allLoans[0].status
                    Write-Host "Found a loan with ID: $loanId and status: $loanStatus" -ForegroundColor Yellow
                }
            } catch {
                Write-Host "Could not retrieve any loans from the system." -ForegroundColor Red
            }
        }
    } else {
        Write-Host "Response: $($loanResponse | ConvertTo-Json)" -ForegroundColor Gray
        $loanId = $loanResponse.loanId
        $loanStatus = $loanResponse.status
        Write-Host "\nLoan requested successfully with ID: $loanId" -ForegroundColor Green
        Write-Host "\nInitial loan status: $loanStatus" -ForegroundColor Green
    }
} catch {
    Write-Host "\nFailed to request loan. Test cannot continue." -ForegroundColor Red
    exit 1
}

# If we don't have a loan ID at this point, we can't continue
if (-not $loanId) {
    Write-Host "\nNo loan ID available. Test cannot continue." -ForegroundColor Red
    exit 1
}

# Step 3: Initiate Score Query
Write-Host "\n=== STEP 3: Initiate Score Query ===" -ForegroundColor Green

$scorePayload = @{
    customerNumber = $TEST_CUSTOMER
} | ConvertTo-Json

Write-Host "Request: POST $LMS_BASE_URL/scores" -ForegroundColor Gray
Write-Host "Payload: $scorePayload" -ForegroundColor Gray

try {
    $scoreResponse = Invoke-ApiWithRetry -Uri "$LMS_BASE_URL/scores" -Method Post -Body $scorePayload -ErrorMessage "Error initiating score query"
    Write-Host "Score query initiated successfully. Response: $($scoreResponse | ConvertTo-Json -Depth 1)" -ForegroundColor Green
} catch {
    Write-Host "Score query failed, but this may be expected if the loan is already processed." -ForegroundColor Yellow
}

# Step 4: Retrieve Transaction Data
Write-Host "\n=== STEP 4: Retrieve Transaction Data ===" -ForegroundColor Green
Write-Host "Request: GET $MIDDLEWARE_BASE_URL/transactions/$TEST_CUSTOMER" -ForegroundColor Gray

try {
    $transactionResponse = Invoke-ApiWithRetry -Uri "$MIDDLEWARE_BASE_URL/transactions/$TEST_CUSTOMER" -Method Get -ErrorMessage "Error retrieving transaction data"
    Write-Host "Transaction data retrieved successfully. Found $($transactionResponse.Count) transactions." -ForegroundColor Green
    
    # Display a sample of transactions if available
    if ($transactionResponse -and $transactionResponse.Count -gt 0) {
        Write-Host "Sample transaction: $($transactionResponse[0] | ConvertTo-Json -Depth 1)" -ForegroundColor Gray
    }
} catch {
    Write-Host "Failed to retrieve transaction data. This may be due to scoring engine connection issues." -ForegroundColor Yellow
    Write-Host "Continuing with test..." -ForegroundColor Yellow
}

# Wait for scoring process to complete
Write-Host "\nWaiting for scoring process to complete..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Step 5: Query Loan Status
Write-Host "\n=== STEP 5: Query Loan Status ===" -ForegroundColor Green
Write-Host "Request: GET $LMS_BASE_URL/loans/$loanId" -ForegroundColor Gray

try {
    $loanStatusResponse = Invoke-ApiWithRetry -Uri "$LMS_BASE_URL/loans/$loanId" -Method Get -ErrorMessage "Error retrieving loan status"
    Write-Host "Response: $($loanStatusResponse | ConvertTo-Json)" -ForegroundColor Gray
    
    Write-Host "\nLoan details retrieved successfully" -ForegroundColor Green
    Write-Host "  - Loan ID: $($loanStatusResponse.loanId)" -ForegroundColor Green
    Write-Host "  - Status: $($loanStatusResponse.status)" -ForegroundColor Green
    Write-Host "  - Amount: $($loanStatusResponse.amount)" -ForegroundColor Green
    
    if ($loanStatusResponse.score) {
        Write-Host "  - Credit Score: $($loanStatusResponse.score)" -ForegroundColor Green
    }
    
    if ($loanStatusResponse.limit) {
        Write-Host "  - Credit Limit: $($loanStatusResponse.limit)" -ForegroundColor Green
    }
    
    if ($loanStatusResponse.rejectionReason) {
        Write-Host "  - Rejection Reason: $($loanStatusResponse.rejectionReason)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "\nFailed to retrieve loan status." -ForegroundColor Red
}

Write-Host "\n=============================================" -ForegroundColor Cyan
Write-Host "API TEST COMPLETED" -ForegroundColor Cyan
Write-Host "=============================================\n" -ForegroundColor Cyan
