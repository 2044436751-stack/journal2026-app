# 日程本 2026 —— 安卓壳

把 `Code_20261002233527.html`（单文件网页日记本）套进一个最小的安卓 App：
页面打进 APK，**完全离线可用**，图标名叫「日程本」。

## 为什么不能直接用"网页转 APK"工具

| 网页转 APK 常见问题 | 这个壳怎么处理的 |
|---|---|
| `file://` 下 localStorage 被禁 → 日记存不住 | 用 `WebViewAssetLoader` 把页面挂到 `https://appassets.androidplatform.net/`，有正常 origin |
| 插图（日历格那个图片按钮）点了没反应 | 实现了 `onShowFileChooser`，能调起相册/文件管理器 |
| 一按返回键就退出、输入全丢 | 返回键先回退页面，连按两次才退出；切后台时先存一次 |
| 跟着系统字体放大，排版散掉 | 锁定 `textZoom=100` |

## 三步拿到 APK（不用在本机装任何开发环境）

### 1. 建一个 GitHub 仓库
- 打开 https://github.com/new ，仓库名随便（比如 `journal2026-app`），**不要勾选** README / .gitignore（本地已经有），点 Create。
- 建好后会显示一个仓库地址，形如 `https://github.com/你的用户名/journal2026-app.git`

### 2. 把这个文件夹推上去
在本文件夹里打开终端（Git Bash / 终端），依次执行：

```bash
git remote add origin https://github.com/你的用户名/journal2026-app.git
git branch -M main
git push -u origin main
```

（不会用命令行也行：GitHub 网页 → 仓库页面 → **Add file → Upload files**，把这个文件夹里的东西全拖进去，注意 `.github` 文件夹也要带上。）

### 3. 跑一次构建，下载 APK
- 进仓库页面 → 上面一排标签点 **Actions**
- 第一次会提示 "I understand my workflows, go ahead and enable them" → 点它
- 左边选 **构建 APK** → 右边 **Run workflow**（绿色按钮）→ 再点一次 Run workflow
- 等 3～8 分钟，变绿后点进去，最下面的 **Artifacts** 里有 `journal2026-debug`
- 下载、解压，得到 `app-debug.apk`

## 装到真我 Neo8

1. 把 `app-debug.apk` 传到手机（微信「文件传输助手」、QQ、USB 拷进去都行）
2. 手机上点这个 apk 安装。realme UI 会拦未知来源：
   设置 → 安全与隐私 → 应用安装授权 → 给对应的来源（微信/浏览器/文件管理）打开开关
3. 装好桌面会出现「日程本」图标，点开就是你的日记本

> 屏幕适配不用操心：Neo8 逻辑宽度约 424dp，页面上限 420px，正好铺满；
> 挖孔屏和底部手势条的安全区页面本身已经处理过（`env(safe-area-inset-bottom)`）。

## 想更新内容怎么办

改完 `Code_20261002233527.html` 后：
1. 覆盖 `app/src/main/assets/journal.html`（就是同一份文件）
2. 把 `app/build.gradle` 里的 `versionCode` 加 1（手机上覆盖安装才认新版本）
3. 提交推送 → 再跑一次 Actions → 重新下载安装

## 关于数据（重要）

- 日记存在 App 自己的私有目录里，**卸载 App 或清除数据就没了**
- 目前没做导出备份，所以手机上的内容和电脑浏览器里那份是**各自独立的**
- 哪天想把电脑上的日记搬进手机，或想做定期备份，跟我说一声，加个「导出 / 导入」按钮就行（十几分钟的事）
