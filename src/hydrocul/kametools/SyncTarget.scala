package hydrocul.kametools;

/**
 * targetDir はrsyncで認識出来るユーザ名@ホスト名:ディレクトリで "/" で終わる。
 */
case class SyncTarget(targetDir: String, sshOption: String);
