# CabmeeHomeApp

CabmeeHomeApp は Android Automotive 向けのホーム画面アプリです。ホーム UI は 2 x 5（合計 10 スロット）を基準としたランチャー構成で、登録済みアプリ起動・隠し操作・設定画面遷移を提供します。

## 主要機能

- **ホーム画面（APPLICATIONS）**
  - 横画面は 5 列、縦画面は 2 列で表示
  - アプリスロットは最大 10 枠（不足分は空スロット）
  - アイコンは **上詰め・左詰め** で配置
- **再起動ボタン**
  - 画面下中央に FAB を表示
  - `android.permission.REBOOT` が許可されていない場合は非表示
- **隠しタップ操作（四隅）**
  - `LT -> RT -> LB -> RB`（3 秒以内）で設定画面を開く
  - 7 タップの既定シーケンスで外部アプリを起動
- **設定画面（SETTINGS）**
  - 戻るアイコンでホームに復帰
  - 自動起動アプリ（ドロップダウン）を選択可能（無しで無効化）
  - 自動起動時間（5/10/20/30/60秒）を選択可能
- **自動起動**
  - `autioStartApplicationIndex`（Nullable）で対象アプリを指定
  - 初期値は `0`（`com.jvckenwood.taitis.taitiscarapp`）
  - ホーム画面表示が30秒継続したら対象アプリを起動（未起動時は起動、バックグラウンド時は前面化）
  - 外部アプリからホームに戻った場合も、ホーム再表示後30秒で再評価して自動起動
  - 設定画面表示中は自動起動しない
  - `null` の場合は自動起動しない
- **テーマ**
  - ライトテーマ: 背景白・文字黒
  - ダークテーマ: 背景ネイビーブルー・文字白

## 画面・状態管理の構成

### Presentation

- `MainActivity`
  - HOME アプリとして起動
  - 戻るキーを無効化
  - Compose ルートとして `NaviScreen` を表示
- `NaviScreen`
  - `hiltViewModel()` で `MainViewModel` を取得
  - Navigation（MAIN / SETTING）を管理
  - `MainScreen` には ViewModel 自体ではなく、必要な状態とイベントハンドラのみ渡す
- `MainScreen`
  - `HomeUiState` の描画
  - アイコングリッド、再起動 FAB、バージョン表示、隠しコーナーボタンを管理
- `SettingScreen`
  - 設定見出しと戻る操作を提供

### Domain / Data

- `InitializeUseCase`
  - 起動時初期化（DataStore からカウンター＋自動起動設定読み込み）
- `UpdateAutoStartAppSettingUseCase`
  - 自動起動設定の更新（StateManager更新＋DataStore永続化）
- `StateManager`
  - `MainState` を StateFlow で保持
- `MainRepository`
  - Proto DataStore を通じてカウンターを保存・読み込み

## 技術スタック

- Kotlin
- Jetpack Compose
- Hilt (Dagger)
- Navigation Compose
- Proto DataStore
- Timber

## ビルド / テスト

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

> 実機・CI で Android SDK が必要です。ローカル実行時は `local.properties` または `ANDROID_HOME` を設定してください。
