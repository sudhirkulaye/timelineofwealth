@echo off
setlocal

REM Copies setup/config files from existing external folders into repository config folder.
REM This does NOT change Java code references yet.

set "REPO_ROOT=C:\MyDocuments\03Business\02workspace\timelineofwealth"
set "TARGET_CONFIG=%REPO_ROOT%\src\main\resources\config"

if not exist "%TARGET_CONFIG%" mkdir "%TARGET_CONFIG%"

echo.
echo Copying files to: %TARGET_CONFIG%
echo.

REM Quarter results / analysis setup files
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\QuarterResultsScreenerExcels\ChartForSheet&Row.property" "%TARGET_CONFIG%\ChartForSheet&Row.property"
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\QuarterResultsScreenerExcels\TickerChartCombination.property" "%TARGET_CONFIG%\TickerChartCombination.property"
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\QuarterResultsScreenerExcels\watchlists.properties" "%TARGET_CONFIG%\watchlists.properties"
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\QuarterResultsScreenerExcels\ReportDataExtractConfig2.0.xlsx" "%TARGET_CONFIG%\ReportDataExtractConfig2.0.xlsx"
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\QuarterResultsScreenerExcels\Analysis\config.properties" "%TARGET_CONFIG%\analysis_config.properties"
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\QuarterResultsScreenerExcels\copy_config.properties" "%TARGET_CONFIG%\analysis_copy_config.properties"
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\QuarterResultsScreenerExcels\Analysis\tickerfolderonfig.properties" "%TARGET_CONFIG%\analysis_tickerfolder_config.properties"

REM Research report setup files
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\ResearchReports\CompanyResearchReports\KotakDaily\config.properties" "%TARGET_CONFIG%\pdf_splitter_config.properties"
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\ResearchReports\CompanyResearchReports\MOSLMorningIndia\config.properties" "%TARGET_CONFIG%\mosl_morningindia_config.properties"
copy /Y "C:\MyDocuments\03Business\05ResearchAndAnalysis\StockInvestments\ResearchReports\CompanyResearchReports\reportconfig.properties" "%TARGET_CONFIG%\report_config.properties"

REM Daily data setup file
copy /Y "C:\MyDocuments\03Business\03DailyData\downloadconfig.property" "%TARGET_CONFIG%\downloadconfig.property"

echo.
echo Done. Review copied files in:
echo   %TARGET_CONFIG%
echo.
echo Next step (manual):
echo   git add src\main\resources\config
echo   git commit -m "Add setup/config files into repository config folder"

echo.
endlocal