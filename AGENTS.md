# ShirokoWear UI — 开发约定

这份文件是"后续继续开发这套 UI"的作业手册。任何一次改动（含 AI 协作）都按此执行。

## 这个库是什么

从 boompala（Wear OS 易学工具）抽出的 Wear OS 设计系统：手感（触觉/转场）、容器、
可视化与页面壳。它服务多个 App，因此**它的正确性由消费方定义**，不由单个 App 的便利定义。

## 铁律

1. **单一真源**。任何 App 里禁止复制本库源码做"本地改良版"。App 需要不同行为时，
   在本库加参数或加可插拔后端；在本库改不动之前，这个需求不算实现完成。
2. **库不含业务语义**。签名里不得出现 engine 类型（`DivinationResult`、`TarotReading`、
   `PulseDiagnosisResult` 等）。业务数据一律由调用方以泛型 + 描述对象传入。
3. **库不含业务文案**。卦名/牌名/脉象名/占题属 App。库自己的字符串放
   `res/values/strings.xml` + `values-en/`，命名一律带 `shirokowear_` 前缀。
4. **库不含大体积资产**。背景视频、牌图、字典数据由 App 自带，库只接 `Uri`/`Painter`。
5. **颜色只从令牌取**。组件里禁止 `Color(0xFF…)` 字面量，一律 `ShirokoWearTheme.colors.*`。
   新增色值先进 `ShirokoWearColors`，再被组件使用。
6. **触觉双门控**。任何震动必须同时读 `LocalShirokoWearHapticFeedbackEnabled` 与
   `LocalShirokoWearHapticIntensity`。直接调 Vibrator 的代码视为缺陷。
   唯一合法豁免是 `ShirokoWearHaptics.preview()`，且只用于两件事：确认"用户刚刚打开
   触觉"、以及切换强度档位时的试听。组件若需要它，必须显式传参开启
   （如 `ShirokoWearToggleCard(confirmEnableAudibly = …)`）且默认关闭——把豁免做成默认
   行为，等于让静音用户每拨一个开关都被震一下，比原来的静默更糟。
7. **不改变已发布行为**。破坏性变更只允许出现在 `-alpha`/`-beta`；正式版之间只加不减。
   旧 API 用 `@Deprecated("Use … instead")` 指向迁移，至少跨 2 个 minor 才升 ERROR 再删。

## API 稳定性

- `explicitApi()` 开着：每个公开声明必须显式 `public`/`internal`，公开函数必须写返回类型。
- 尚未定型的能力（页面壳、揭示状态机、流式卡片）必须标 `@UnstableShirokoWearApi`，
  等第二个消费方验证后再去掉注解晋升为稳定 API。
- 逃生舱用 `LocalShirokoWearAnimationsEnabled` / `LocalShirokoWearRotaryScrollingEnabled`
  这类 local，**不要**为单个 App 加布尔参数穿透三层调用。

## 加一个组件的标准工序

1. 库仓先开 issue，写明"来自哪个 App 的哪个真实屏幕"。没有真实需求来源不做。
2. 实现放 `shirokowear/src/main/.../ui/`，令牌只从 `ShirokoWearTheme` 取。
3. 补 JVM 单测：断言**用户可观察结果**（圆/方屏对齐、字号档换算、动画关闭后的降级、
   命中区边界内外），不要只断言常量和公式。
4. 在 `:sample` 加一屏 gallery，一组件一屏。gallery 是本库唯一的视觉回归台。
5. 表冠/拖拽/震动等真实交互补 `androidTest`，并注明需真机或 Wear 模拟器执行。
6. 更新 `CHANGELOG.md` 与 README 兼容表（若依赖版本变化）。
7. 版本号只在 `gradle/libs.versions.toml` 的 `version` / `mavenLocalVersion` 两处改。

## 验证边界（不得自欺）

- 纯计算（尺寸换算、缩放参数、转场分支、波形几何）→ JVM 可断言。
- 表冠焦点、震动波形、圆屏裁切、帧率 → **没有真手表/模拟器跑过就不许写"已验证"**。
  Release note 固定三段：`已 JVM 验证` / `已在 <型号> 真机验证` / `未验证`。
- 三星专有触觉依赖厂商 HAL，非三星设备无法复现同一手感，这是设计事实不是 bug。

## 发布

- 日常开发：**不要靠发版验证**。消费方用 `includeBuild` + `dependencySubstitution`
  直连本库源码。
- `publishToMavenLocal` 自动使用 `mavenLocalVersion`（SNAPSHOT 线），避免与
  Central 上不可变的正式版撞号。
- 版本号的唯一真源是 `gradle/libs.versions.toml`：`version` = 正式版线，
  `mavenLocalVersion` = 本地与快照线。打 tag `vX.Y.Z` 时 `X.Y.Z` 必须与 `version`
  一致（release workflow 用 `${GITHUB_REF_NAME#v}` 覆盖 `publicationVersion`）。

### CI 三条轨道

| workflow | 触发 | 做什么 |
|---|---|---|
| `verify.yml` | 任意分支 push / PR / 被复用 | 单测 + `koverVerify` 门禁 + 出 AAR 与 gallery APK |
| `publish-snapshot.yml` | main 合并 | `-PsnapshotPublication=true` 发 SNAPSHOT 线到 **GitHub Packages**（不签名、可覆盖） |
| `release.yml` | 打 `v*` tag | `publishAndReleaseToMavenCentral` 发 **Maven Central**（需签名，不可覆盖） |

两个 publish 轨道都 `needs: verify`，绕不开门禁。

- **任务名绑死在 vanniktech 0.30.0**：`publishAndReleaseToMavenCentral`（0.37 起改叫
  `publishToMavenCentral` + `mavenCentralAutomaticPublishing`）。升级插件必须同时改两个
  workflow，否则 CI 会静默不发布或直接失败。
- Central 需要签名与 sources/javadoc：本地无 `signingInMemoryKey` 时根构建自动跳过签名，
  CI 里通过 `ORG_GRADLE_PROJECT_signingInMemoryKey` 等变量开启。
- **正式版发布前置条件**：至少一个消费 App 在源码轨跑过一轮，且真机项已按型号确认。
  否则只发快照。

### 覆盖率门禁为什么要求"纯核"

`koverVerify` 只统计 `ShirokoWearDimens*`、`ShirokoWearHapticWaveform*`、
`ShirokoWearAmbientPalette*`（以及 navigation 里的 `ShirokoWearRouteKt*`），门槛 80%。

因此有一条结构约束：**被门禁统计的文件里不得有 `android.*` import，也不得有 `@Composable`**。
门禁两次踩过同一个坑（触觉表 + `introStepDirection`）：纯函数与 Composable 或 Android
副作用同文件时，覆盖率分母包含永不可测的行，44% / 51% 的失败其实是在提醒你文件切错了。
新增纯逻辑一律与 UI/副作用分文件——这不是风格，是让门禁有意义的前提。

同理，**不要**用 JVM 测试去断言 Compose 快照调度器（例如 `derivedStateOf` 何时失效）：
同一份源码在 debug 变体通过、release 变体失败，那种测试日后只会被删掉。只断言本库自己
拥有的映射与状态变更。

## 许可与归属

- 本库 Apache-2.0，新文件带版权头。
- 移植 Lucide 派生图标时必须补 `NOTICE`（ISC 要求保留版权与许可声明），并在文件头注明
  "derived from Lucide (ISC)"。
- 永远不要把 CC BY-SA 4.0 的第三方文本（如爻辞数据）带进本仓，ShareAlike 会传染整个库。

## 提交

- commit message 必须含简短标题 + **非空正文**（改了什么、为什么、如何验证）。禁止只有标题。
- 提交前跑：`./gradlew --no-daemon :shirokowear:testDebugUnitTest :sample:assembleDebug`。
- 一次提交只做一件事，与发布节奏对齐（一个组件/一个手感修正/一次依赖升级）。
