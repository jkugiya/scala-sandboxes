package jkugiya.guice_sample

import com.google.inject.AbstractModule

/**
  * Created by jkugi_000 on 2015/11/28.
  */
class ServiceModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Gateway]).to(classOf[GatewayImpl])
    bind(classOf[RecognitionService]).to(classOf[RecognitionServiceImpl])
  }
}
