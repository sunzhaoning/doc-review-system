# 文档评审系统 - 项目计划 (v2 - 重启)

## 项目概述
- **目标**：构建文档评审系统，支持上传、评审流程、意见收集、归档
- **工期**：1天（重启时间：2026-03-05 16:35）
- **核心功能**：文档上传、评审流程、评审意见、归档
- **集成**：AD域认证
- **用户**：研发人员
- **GitHub**：https://github.com/sunzhaoning/doc-review-system

## 审核要求
以下产出需要 @admin 审核：
- [ ] 需求文档（product-lead输出后）
- [ ] 设计方案（ux-designer输出后）
- [ ] 技术架构（fullstack-dev输出后）

## 阶段规划

### 阶段0：项目初始化
- [x] Manager: 创建GitHub仓库 ✅ 2026-03-05 16:35
- [ ] 所有成员: 克隆仓库到本地工作目录
- [ ] 建立项目目录结构

### 阶段1：需求与设计 (2小时)
- [x] product-lead: 输出需求文档（用户故事、功能清单、验收标准） ✅ 2026-03-05 16:58
- [~] ⏸️ **审核点**：@admin 确认需求文档 (待审核)
- [ ] ux-designer: 输出交互原型/设计稿
- [ ] ⏸️ **审核点**：@admin 确认设计方案
- [ ] product-lead: 确认AD域集成方案

### 阶段2：技术方案 (1小时)
- [ ] fullstack-dev: 输出技术架构设计
- [ ] ⏸️ **审核点**：@admin 确认技术方案
- [ ] fullstack-dev: 推送技术方案到GitHub

### 阶段3：开发 (5小时)
- [ ] fullstack-dev: 搭建项目骨架，推送到GitHub
- [ ] fullstack-dev: 实现文档上传功能
- [ ] fullstack-dev: 实现评审流程引擎
- [ ] fullstack-dev: 实现评审意见功能
- [ ] fullstack-dev: 实现归档功能
- [ ] fullstack-dev: 集成AD域认证
- [ ] **每个功能完成后推送到GitHub**

### 阶段4：测试与文档 (2小时)
- [ ] qa-automation: 编写测试用例，推送到GitHub
- [ ] qa-automation: 执行功能测试
- [ ] technical-writer: 编写API文档，推送到GitHub
- [ ] technical-writer: 编写用户手册，推送到GitHub

## 里程碑
- [x] M0: GitHub仓库创建完成 ✅ 2026-03-05 16:35
- [ ] M1: 需求文档审核通过
- [ ] M2: 设计方案审核通过
- [ ] M3: 技术方案审核通过
- [ ] M4: 核心功能开发完成并推送
- [ ] M5: 测试通过，文档完成并推送

## Git提交规范
- `docs:` 文档更新
- `feat:` 新功能
- `fix:` 修复
- `test:` 测试用例
- `refactor:` 重构

## 备注
- 所有产出必须推送到GitHub
- 关键文档必须经@admin审核后才能进入下一阶段
- 每个阶段完成后@老弟汇报
