@echo off
chcp 65001 >nul
title 星火志愿服务平台 - 启动

echo ================================================================================
echo   星火志愿服务平台
echo   Spark Volunteer Platform
echo ================================================================================
echo.

:: ---------------------------------------------------------------------------
:: 检查 .env
:: ---------------------------------------------------------------------------
if not exist ".env" (
    echo [错误] 未找到 .env 文件
    echo.
    echo   请执行:  copy .env.example .env
    echo   然后编辑 .env 中的 DB_PASSWORD / JWT_SECRET / DOMAIN
    echo.
    pause & exit /b 1
)

:: ---------------------------------------------------------------------------
:: 检查 HTTPS 证书是否已配置
:: ---------------------------------------------------------------------------
findstr /C:"YOUR_DOMAIN" nginx.conf >nul 2>&1
if %errorlevel% equ 0 (
    echo [警告] nginx.conf 中 YOUR_DOMAIN 尚未替换为真实域名
    echo.
    echo   请先运行证书配置脚本:
    echo     bash certbot-setup.sh your-domain.com
    echo.
    echo   或跳过 HTTPS 仅启动后端和数据库:
    echo     docker-compose up -d mysql app
    echo.
    pause & exit /b 1
)

:: ---------------------------------------------------------------------------
:: 启动
:: ---------------------------------------------------------------------------
echo 正在启动全部服务 ...
docker-compose up -d

echo.
echo ================================================================================
echo   服务启动完成
echo.
echo   查看日志:
echo     docker-compose logs -f app
echo     docker-compose logs -f nginx
echo.
echo   访问地址: https://%DOMAIN%
echo ================================================================================
echo.
pause
