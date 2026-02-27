# Agents.md

このドキュメントは CabmeeHomeApp リポジトリを変更するエージェント向けの作業ガイドです。

## 1. プロジェクト概要

- 対象: Android Automotive 向けホームアプリ
- UI: Jetpack Compose
- DI: Hilt
- アーキテクチャ: **Clean Architecture**
- 画面:
  - `MAIN_SCREEN`（ホーム）
  - `SETTING_SCREEN`（設定）

## 2. アーキテクチャ方針（必須）

1. 本アプリは **Clean Architecture** で構成する。
2. 機能仕様・ビジネスロジックは **Domain 層**（usecase / entity / state / interface）に記載・実装する。
3. Presentation 層（screen / viewmodel）は UI 表示と UI イベント処理に責務を限定する。
4. Data 層は Repository 実装・永続化・外部データアクセスのみを担当する。
5. 画面間受け渡しでは ViewModel インスタンスを直接渡さず、必要な状態データとイベントハンドラを渡す。

## 3. 実装上の重要ルール

1. ホーム画面は **2 x 5（最大10スロット）** を前提とする。
2. アイコングリッドは余白があっても **上詰め・左詰め** で配置する。
3. `REBOOT` 権限がない端末では再起動 FAB を表示しない。
4. ViewModel の取得は `MainActivity` ではなく `NaviScreen`（`hiltViewModel()`）で行う。
5. `MainScreen` へ ViewModel を直接渡さず、必要な `uiState` とコールバックのみを渡す。
6. ダークテーマ時は背景ネイビーブルー・文字色白を維持する。
7. 自動起動設定 `autioStartApplicationIndex` は Nullable とし、`null` の場合は自動起動を無効化する。
8. ホーム画面表示が30秒継続した際に自動起動対象を前面化する要件を維持する。

## 4. レイヤ構成

- `presentation/view/MainActivity.kt`
  - Activity エントリー
- `presentation/view/screen/NaviScreen.kt`
  - Navigation と画面間イベント橋渡し
- `presentation/view/screen/MainScreen.kt`
  - ホーム画面描画
- `presentation/view/screen/SettingScreen.kt`
  - 設定画面描画
- `presentation/viewmodel/MainViewModel.kt`
  - 状態とイベント処理
- `domain/usecase/InitializeUseCase.kt`
  - 初期化処理
- `data/repository/MainRepository.kt`
  - DataStore I/O

## 5. 動作確認

優先チェック:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Android SDK がない環境では `sdk.dir` 未設定エラーになるため、検証環境要件を明記すること。

## 6. 変更時のドキュメント更新

以下に影響がある変更を行った場合は更新する:

- 画面構成 / Navigation
- ViewModel の取得方法
- 表示条件（権限依存表示、テーマ対応など）
- レイヤ責務（Clean Architecture）
- ビルド・実行手順

更新対象:

- `README.md`
- `Agents.md`
