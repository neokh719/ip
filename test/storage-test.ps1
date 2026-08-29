$ErrorActionPreference = "Stop"

$repositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot ".."))
$testDirectory = Join-Path ([IO.Path]::GetTempPath()) ("plana-storage-test-" + [guid]::NewGuid().ToString("N"))
$classesDirectory = Join-Path $testDirectory "classes"

New-Item -ItemType Directory -Path $classesDirectory -Force | Out-Null
Push-Location $testDirectory

try {
    $sourceFiles = @(Get-ChildItem -LiteralPath (Join-Path $repositoryRoot "src/main/java") -Filter *.java -File -Recurse |
        ForEach-Object { $_.FullName })
    & javac -d $classesDirectory @sourceFiles
    if ($LASTEXITCODE -ne 0) {
        throw "Compilation failed."
    }

    $input = @"
todo buy milk
deadline submit report /by Friday
event team meeting /from 2pm /to 3pm
mark 1
delete 2
bye
"@
    $output = $input | & java -cp $classesDirectory Plana
    if ($LASTEXITCODE -ne 0) {
        throw "Plana exited with code $LASTEXITCODE."
    }

    $dataFile = Join-Path $testDirectory "data/plana.txt"
    if (-not (Test-Path -LiteralPath $dataFile -PathType Leaf)) {
        throw "Expected save file was not created: $dataFile"
    }

    $actual = [IO.File]::ReadAllText($dataFile)
    $expected = "T | 1 | buy milk`nE | 0 | team meeting | 2pm | 3pm`n"
    $actual = $actual -replace "`r`n|`r", "`n"
    if ($actual -cne $expected) {
        throw "Saved file contents were incorrect.`nExpected:`n$expected`nActual:`n$actual"
    }

    Write-Output "Storage test passed."
}
finally {
    Pop-Location
    if (Test-Path -LiteralPath $testDirectory) {
        Remove-Item -LiteralPath $testDirectory -Recurse -Force -ErrorAction SilentlyContinue
    }
}
