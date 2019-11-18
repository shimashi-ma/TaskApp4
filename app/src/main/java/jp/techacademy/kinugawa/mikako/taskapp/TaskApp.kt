package jp.techacademy.kinugawa.mikako.taskapp

import android.app.Application
import io.realm.Realm

//モデルクラスTaskと接続する、Realmデータベースを用意する
//Applicationクラスを継承したTaskAppクラスを作成
//特別な設定を行わずデフォルトの設定を使う場合、下記のように記述

class TaskApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)  //Realmを初期化
    }
}