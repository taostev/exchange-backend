# 拾光交换所

校园闲置物品交换平台，包含 Spring Boot 后端与 React 前端。

## 目录

- `src/`：Spring Boot 后端（含 Gemini LLM 智能推荐）
- `frontend/`：React + Vite 前端
- `sql/`：数据库初始化与演示数据脚本

## 启动

1. 启动 MySQL，并创建 `exchange_db`（默认账号密码均为 `root`）。
2. 在后端根目录运行：`mvn spring-boot:run`
3. 在前端目录运行：`cd frontend && npm install && npm run dev`
4. 浏览器访问 `http://localhost:5173`

前端开发服务器会把 `/api` 和 `/uploads` 代理到 `http://localhost:8080`。

默认管理员账号：`admin` / `admin123`

## 录屏演示数据

可手动导入 `sql/demo-data.sql` 获得 6 件展示物品、关注记录和交换订单。

- 管理员：`admin` / `admin123`
- 演示用户小林：`xiaolin_demo2026` / `demo123`
- 演示用户陈宇：`chenyu_demo2026` / `demo123`

## 智能推荐

- 接口：`GET /api/recommend`
- 登录用户优先走 Gemini 大模型推荐，失败时自动降级为规则推荐
- 首页展示推荐来源与推荐理由
