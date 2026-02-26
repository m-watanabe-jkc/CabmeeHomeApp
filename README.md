# CabmeeHomeApp

CabmeeHomeApp は Android Automotive 向けのホームアプリです。Compose でアプリランチャー画面を描画し、指定されたアプリの起動や再起動ダイアログを提供します。

## コード構成（現状分析）

### エントリーポイント
- `MainActivity` はホームアプリとしての Activity 初期化に責務を限定しています。
- 主な役割は以下です。
  - 戻るボタンの無効化（ホーム用途）
  - Compose のルート `HomeScreen` の表示
  - 画面向けメッセージ表示（Toast）の受け口

### Screen 層
- `ui/home/HomeScreen.kt` に UI 描画ロジックを分離しています。
- 主な責務は以下です。
  - タイトル、グリッド、リブート FAB、隠しタップ領域、バージョン表示の描画
  - タップイベントからのアプリ起動要求
  - 再起動確認ダイアログの表示制御

### ViewModel 層
- `ui/home/MainViewModel.kt` を追加し、画面表示向けデータを管理しています。
- 主な責務は以下です。
  - 表示対象パッケージ一覧の保持
  - 10 スロット固定化した一覧データの生成
  - PackageManager からのアプリラベル/アイコンの取得と `uiState` への反映
  - 隠しタップシーケンス判定（設定アプリ/ファイルマネージャ起動用）

### そのほか
- `HardwareKeyDetectionService.kt` はハードウェアキー関連処理のサービス実装です。
- `ui/theme/*` は Compose テーマ定義です。

## 画面仕様（要点）
- 縦画面: 2 列、横画面: 5 列
- 常に 10 スロットを表示（不足分は空スロット）
- 隠しタップシーケンスで特定アプリを起動
- FAB から再起動確認ダイアログを表示

## ビルド・チェック
```bash
./gradlew :app:assembleDebug
./gradlew test
```
