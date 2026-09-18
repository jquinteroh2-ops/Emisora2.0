@echo off
chcp 65001 > nul
title Emisora 2.0 - Spring Boot MVC con Thymeleaf

echo =====================================================================
echo  Emisora 2.0 - Ejercicio 25: Sistema de Gestión de Emisoras Radiales
echo  Desarrollo Web - Unidad 2 | Jose Antonio Quintero Herrera (7502510055)
echo =====================================================================
echo.
echo Iniciando aplicación con Spring Boot y base de datos H2 en memoria...
echo La aplicación estará disponible en: http://localhost:8080/
echo.
echo Credenciales iniciales de prueba:
echo   - ADMIN:    U001  /  Admin2026*
echo   - OPERADOR: U002  /  Admin2026*
echo   - CONSULTA: U004  /  Admin2026*
echo.
echo Presione Ctrl + C para detener el servidor.
echo =====================================================================
echo.

call mvn spring-boot:run
pause
