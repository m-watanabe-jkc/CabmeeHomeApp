# Agents Guide

このリポジトリで作業するエージェント向けのメモです。

## プロジェクト概要
- Android (Jetpack Compose) ベースのホームアプリです。
- 画面はホーム (`HomeScreen`) と設定 (`SettingScreen`) の 2 画面です。

## 推奨レイヤリング
- `MainActivity`: Activity 初期化・画面ルーティングのみ
- `Screen`: Compose UI 描画とユーザー操作
- `ViewModel`: 画面状態の生成と保持、表示データ管理、隠し操作判定

## 実装ルール
1. 画面表示データ（リスト、表示名、状態）は ViewModel で管理する。
2. Activity に業務ロジックを増やさない。
3. UI コンポーネントは Screen ファイルへ分離する。
4. 例外処理メッセージは日本語ユーザー向けの文言を維持する。
5. 隠し操作のタイムアウトと順序判定は ViewModel 側で一元管理する。

## 現在の隠し操作仕様
- 四隅ボタン: LT(左上), RT(右上), LB(左下), RB(右下)
- 3秒以内に `LT -> RT -> LB -> RB` でアプリ内設定画面へ遷移

## 確認コマンド
```bash
./gradlew :app:assembleDebug
./gradlew test
```
