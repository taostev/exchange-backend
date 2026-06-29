# 拾光交换所

校园闲置物品交换平台，包含 Spring Boot 后端与 React 前端。

## 目录

- `src/`：Spring Boot 后端（含 Gemini LLM 智能推荐）
- `frontend/`：React + Vite 前端
- `sql/`：数据库初始化与演示数据脚本
- `docker-compose.yml`：MySQL 容器（可选）
- `scripts/`：本地启动脚本

## 开发环境

| 组件 | 版本/说明 |
|------|-----------|
| JDK | 21+（见 `pom.xml`） |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL | 5.7 / 8.0，库名 `exchange_db` |
| 浏览器 | Chrome / Edge 等现代浏览器 |

**推荐本地方案**：使用 phpStudy / 小皮面板启动 MySQL，Java 后端 + Vite 前端分别运行。  
**备选方案**：使用 `docker compose up -d` 启动 MySQL 容器。

默认数据库配置（`src/main/resources/application.yaml`）：

- 地址：`localhost:3306`
- 数据库：`exchange_db`
- 用户名 / 密码：`root` / `root`

Gemini 推荐密钥可在 `application.yaml` 中配置，或通过 `application-local.yaml` 本地覆盖（该文件不会提交到 Git）。

## 本地启动

```bash
# 1. 初始化数据库（首次）
mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS exchange_db DEFAULT CHARACTER SET utf8mb4;"
mysql -uroot -proot exchange_db < sql/init.sql

# 2. 启动后端（项目根目录）
mvn spring-boot:run
# 或使用脚本（需先启动 MySQL）
./scripts/start-phpstudy.sh

# 3. 启动前端（新终端）
cd frontend && npm install && npm run dev
```

4. 浏览器访问 `http://localhost:5173`

前端开发服务器会把 `/api` 和 `/uploads` 代理到 `http://localhost:8080`。

**常用地址**

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:5173 |
| 后端 API | http://localhost:8080 |
| 接口文档 | http://localhost:8080/doc.html |

默认管理员账号：`admin` / `admin123`

## 系统部署（简要）

适用于答辩演示、校内服务器或云主机部署。

### 1. 准备环境

在服务器安装 JDK 21、Maven、Node.js、MySQL（或 Docker），开放所需端口（通常 80/443，后端 8080）。

### 2. 部署数据库

```bash
mysql -uroot -p -e "CREATE DATABASE exchange_db DEFAULT CHARACTER SET utf8mb4;"
mysql -uroot -p exchange_db < sql/init.sql
# 可选：导入演示数据
mysql -uroot -p exchange_db < sql/demo-data.sql
```

修改 `src/main/resources/application.yaml` 中的数据库连接信息；生产环境建议使用独立账号，不要使用 `root`。

### 3. 部署后端

```bash
mvn -DskipTests package
java -jar target/exchange-0.0.1-SNAPSHOT.jar
```

确保运行目录下存在 `uploads/` 目录（用于物品图片上传），并保证进程对 MySQL 可访问。

### 4. 部署前端

```bash
cd frontend
npm install
npm run build
```

将 `frontend/dist/` 作为静态站点发布：

- **方式 A**：Nginx 托管 `dist/`，并将 `/api`、`/uploads` 反向代理到 `http://127.0.0.1:8080`
- **方式 B**：本地演示可直接 `npm run preview` 临时预览构建结果

### 5. 部署结构示意

```text
用户浏览器
    ↓
Nginx（80/443）
    ├─ /          → frontend/dist 静态文件
    ├─ /api       → Spring Boot :8080
    └─ /uploads   → Spring Boot :8080
         ↓
      MySQL :3306
```

### 6. 上线注意

- 修改默认管理员密码，关闭不必要的调试配置
- Gemini API Key 建议使用环境变量或 `application-local.yaml`，不要硬编码在公开仓库
- 定期备份 `exchange_db` 与 `uploads/` 目录

## 录屏演示数据

可手动导入 `sql/demo-data.sql` 获得 6 件展示物品、关注记录和交换订单。

- 管理员：`admin` / `admin123`
- 演示用户小林：`xiaolin_demo2026` / `demo123`
- 演示用户陈宇：`chenyu_demo2026` / `demo123`

## 智能推荐

- 接口：`GET /api/recommend`
- 登录用户优先走 Gemini 大模型推荐，失败时自动降级为规则推荐
- 首页展示推荐来源与推荐理由

更多后端细节见 [README-LOCAL.md](./README-LOCAL.md)。
