package hydrocul.kametools;

case class HelpApp(app: App) extends App {

  override def exec(args: Array[String], env: App.Env){

    app.help(env);

  }

  override def help(env: App.Env){

    env.out.println("display help");
    env.out.println("usage: ");

  }

}
