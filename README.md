# CabmeeHomeApp

CabmeeHomeApp は Android Automotive 向けのホームアプリです。Compose でホーム画面と設定画面を描画し、指定アプリの起動や隠し操作による画面遷移を提供します。

## コード構成（現状分析）

### エントリーポイント
- `MainActivity` はホームアプリとしての Activity 初期化と、画面ルート（HOME / SETTINGS）の切り替えだけを担当します。
- 主な役割:
  - 戻るボタンの無効化（ホーム用途）
  - `HomeScreen` / `SettingScreen` の出し分け
  - Toast 表示の受け口

### Screen 層
- `ui/home/HomeScreen.kt`
  - タイトル、アプリグリッド、リブート FAB、バージョン表示を描画
  - 隠しボタン（LT/RT/LB/RB）を配置
  - 隠しシーケンスの結果に応じたアプリ起動 / 設定画面遷移を実行
- `ui/home/SettingScreen.kt`
  - 左詰めタイトル `SETTINGS` と水平区切り線を描画
  - タイトル左の ← アイコン押下でホーム画面へ復帰

### ViewModel 層
- `ui/home/MainViewModel.kt`
  - ホーム画面の表示データ（10スロット、ラベル、アイコン、バージョン）を管理
  - 隠し操作シーケンスを 3 秒以内で判定
    - `LT -> RT -> LB -> RB` でアプリ内設定画面へ遷移
    - 既存の 7 タップシーケンスで外部アプリ起動

### そのほか
- `HardwareKeyDetectionService.kt` はハードウェアキー関連のサービス実装
- `ui/theme/*` は Compose テーマ定義

## 画面仕様（要点）
- 縦画面: 2 列、横画面: 5 列
- 常に 10 スロット表示（不足分は空スロット）
- 隠しボタンを四隅に配置
- 3 秒以内の `LT -> RT -> LB -> RB` で設定画面に遷移
- 設定画面は ← アイコンでホームに戻る

## ビルド・チェック
```bash
./gradlew :app:assembleDebug
./gradlew test
```
