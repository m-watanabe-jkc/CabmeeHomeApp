# Agents Guide

このリポジトリで作業するエージェント向けのメモです。

## プロジェクト概要
- Android (Jetpack Compose) ベースのホームアプリです。
- UI は `app/src/main/java/com/jvckenwood/cabmee/homeapp/ui/home` に配置しています。

## 推奨レイヤリング
- `MainActivity`: Activity 初期化・ナビゲーション起点のみ
- `Screen`: Compose UI の描画とユーザー操作
- `ViewModel`: 画面状態の生成と保持、表示データ管理

## 実装ルール
1. 画面に表示するデータ（リスト、表示名、状態）は ViewModel で管理する。
2. Activity に業務ロジックを増やさない。
3. UI コンポーネントは Screen ファイルへ分離する。
4. 例外処理メッセージは日本語ユーザー向けの文言を維持する。

## 確認コマンド
```bash
./gradlew :app:assembleDebug
./gradlew test
```
