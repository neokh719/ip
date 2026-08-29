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
deadline submit report /by 2019-10-15
event team meeting /from 2019-10-15 /to 2019-10-16
mark 1
delete 2
bye
"@
    $output = $input | & java -cp $classesDirectory plana.Plana
    if ($LASTEXITCODE -ne 0) {
        throw "Plana exited with code $LASTEXITCODE."
    }

    $dataFile = Join-Path $testDirectory "data/plana.txt"
    if (-not (Test-Path -LiteralPath $dataFile -PathType Leaf)) {
        throw "Expected save file was not created: $dataFile"
    }

    $actual = [IO.File]::ReadAllText($dataFile)
    $expected = "T | 1 | buy milk`nE | 0 | team meeting | 2019-10-15 | 2019-10-16`n"
    $actual = $actual -replace "`r`n|`r", "`n"
    if ($actual -cne $expected) {
        throw "Saved file contents were incorrect.`nExpected:`n$expected`nActual:`n$actual"
    }

    $corruptedRecords = @(
        "corrupted record",
        "T | 2 | invalid status",
        "D | 0 | missing date |",
        "E | 0 | missing end | 2pm |",
        "T | 0 | escaped \| pipe and \\ slash"
    )
    $corruptedText = [string]::Join([Environment]::NewLine, $corruptedRecords) + [Environment]::NewLine
    [IO.File]::AppendAllText($dataFile, $corruptedText, [Text.UTF8Encoding]::new($false))
    $secondInput = @"
list
bye
"@
    $secondOutput = (($secondInput | & java -cp $classesDirectory plana.Plana) -join "`n")
    if ($LASTEXITCODE -ne 0) {
        throw "Plana exited with code $LASTEXITCODE while loading."
    }
    if (-not $secondOutput.Contains("1.[T][X] buy milk") -or
        -not $secondOutput.Contains("2.[E][ ] team meeting (from: Oct 15 2019 to: Oct 16 2019)") -or
        -not $secondOutput.Contains("3.[T][ ] escaped | pipe and \ slash")) {
        throw "Loaded task contents were incorrect."
    }

    Write-Output "Storage save/load test passed."
}
finally {
    Pop-Location
    if (Test-Path -LiteralPath $testDirectory) {
        Remove-Item -LiteralPath $testDirectory -Recurse -Force -ErrorAction SilentlyContinue
    }
}
