# exchange-backend 本地部署指南

Spring Boot 4 + Java 25 + MySQL + MyBatis 的二手物品交换平台后端。

## 环境要求

| 组件 | 版本/说明 |
|------|-----------|
| JDK | 25（项目 `pom.xml` 指定） |
| Maven | 3.9+ |
| MySQL | 8.0，库名 `exchange_db` |
| phpStudy/小皮面板 | 推荐只使用其中的 MySQL |
| Docker | 备选，用于一键启动 MySQL |

## 推荐方案：使用 phpStudy 的 MySQL

根据 `xchsdo/phpStudy` 的 README，phpStudy 是 Windows 集成环境，包含 `PHP / MySql / Apache / Nginx / Redis / FTP / Composer`。本项目是 Java Spring Boot 后端，只需要 MySQL，不需要 PHP、Apache、Nginx。

当前最适合的方案是：在 phpStudy/小皮面板里启动 MySQL，然后让本项目连接 `localhost:3306`。

```bash
cd /Users/admin/Desktop/题库/exchange-backend

# 1. 在 phpStudy/小皮面板中启动 MySQL

# 2. 创建数据库并导入表结构
mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS exchange_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -proot exchange_db < sql/init.sql

# 3. 启动后端
./scripts/start-phpstudy.sh
```

默认数据库配置见 `src/main/resources/application.yaml`：

- 地址：`localhost:3306`
- 数据库：`exchange_db`
- 用户名：`root`
- 密码：`root`

如果你的 phpStudy MySQL 密码不是 `root`，请同步修改 `application.yaml`。

## 备选方案：使用 Docker MySQL

```bash
cd /Users/admin/Desktop/题库/exchange-backend
docker compose up -d
docker exec exchange-mysql mysqladmin ping -uroot -proot
mvn spring-boot:run
```

## 数据库配置

默认配置见 `src/main/resources/application.yaml`：

- 地址：`localhost:3306`
- 数据库：`exchange_db`
- 用户名：`root`
- 密码：`root`

若使用 phpStudy/小皮面板等本地 MySQL，请修改 `application.yaml` 中的 `spring.datasource` 配置，并手动执行 `sql/init.sql` 初始化表结构。

## 初始化表结构

Docker 首次启动会自动执行 `sql/init.sql`，创建：

- `sys_user` — 用户表
- `busi_item` — 物品表

手动导入：

```bash
mysql -uroot -proot < sql/init.sql
```

## 访问地址

| 服务 | 地址 |
|------|------|
| API 基础地址 | http://localhost:8080 |
| Knife4j 文档 | http://localhost:8080/doc.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

## 常用 API

- 用户注册：`POST /user/register`
- 用户登录：`POST /user/login`

## 停止服务

```bash
# 停止 Spring Boot：Ctrl+C

# 停止 MySQL 容器
docker compose down
```

## 常见问题

**1. 端口 3306 被占用**

修改 `docker-compose.yml` 端口映射，例如 `"3307:3306"`，并同步修改 `application.yaml` 中的 JDBC URL。

**2. Maven 依赖下载慢**

可配置国内镜像，编辑 `~/.m2/settings.xml` 添加阿里云仓库。

**3. Java 版本不匹配**

确保 `java -version` 输出为 25.x。
