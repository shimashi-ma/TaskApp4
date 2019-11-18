package jp.techacademy.kinugawa.mikako.taskapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.Date

//RealmのモデルクラスTaskを作成　モデルとはデータを表現するもの
//open修飾子を付けるのは、Realmが内部的にTaskを継承したクラスを作成して利用するため
//Serializableインターフェイス 生成したオブジェクトをシリアライズできる
//シリアライズ　データを丸ごとファイルに保存したり、TaskAppでいうと別のActivityに渡すことができるようにすること
open class Task: RealmObject(), Serializable {

    var title: String = ""     //タイトル
    var contents: String = ""   //内容
    var category: String = ""   //カテゴリ
    var date: Date = Date()     // 日時

    // id をプライマリーキーとして設定
    //@PrimaryKeyはRealmがプライマリーキーと判断するために必要
    @PrimaryKey
    var id: Int = 0
}