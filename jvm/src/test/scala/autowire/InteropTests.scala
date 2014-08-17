package autowire
import utest._
import scala.concurrent.Future
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import utest.util.Tree
import utest.framework.Test
import utest.ExecutionContext.RunNow
import scala.reflect.ClassTag
import utest._
import scala.pickling._
import json._
import org.objenesis.strategy.StdInstantiatorStrategy


object OtherTests extends TestSuite{
  import utest.PlatformShims.await
  println(utest.*)
  val tests = TestSuite {
    'reflection{

      trait Rw{
        def write[T](t: T) = {
          val buffer = new ByteArrayOutputStream()
          val oos = new ObjectOutputStream(buffer)
          oos.writeObject(t)
          oos.flush()
          oos.close()
          new String(buffer.toByteArray)
        }
        def read[T](s: String) = {
          val in = new ByteArrayInputStream(s.getBytes)
          val ois = new ObjectInputStream(in)
          val obj = ois.readObject()
          obj.asInstanceOf[T]
        }
      }

      object Server extends autowire.Server with Rw{
        val routes = route[Api](Controller)
      }

      object Client extends autowire.Client with Rw{
        case class NoSuchRoute(msg: String) extends Exception(msg)
        def callRequest(r: Request) = {
          Server.routes
            .lift(r)
            .getOrElse(Future.failed(new NoSuchRoute("No route found : " + r.path)))
        }
      }



      val res1 = await(Client[Api].add(1, 2, 3).call())
      val res2 = await(Client[Api].add(1).call())
      val res3 = await(Client[Api].add(1, 2).call())
      val res4 = await(Client[Api].multiply(x = 1.2, Seq(2.3)).call())
      val res5 = await(Client[Api].multiply(x = 1.1, ys = Seq(2.2, 3.3, 4.4)).call())

      assert(
        res1 == "1+2+3",
        res2 == "1+2+10",
        res3 == "1+2+10",
        res4 == "1.2*2.3",
        res5 == "1.1*2.2*3.3*4.4"
      )
    }

    'kryo {
      trait Rw{
        val kryo = new com.esotericsoftware.kryo.Kryo()
        kryo.setRegistrationRequired(false)
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy())
        kryo.register(classOf[scala.collection.immutable.::[_]],60)

        def write[T: ClassTag](t: T): String = {
          val output = new com.esotericsoftware.kryo.io.Output(new ByteArrayOutputStream())
          kryo.writeClassAndObject(output, t)
          new String(output.toBytes)
        }
        def read[T: ClassTag](s: String): T = {
          val input = new com.esotericsoftware.kryo.io.Input(new ByteArrayInputStream(s.getBytes))
          kryo.readClassAndObject(input).asInstanceOf[T]
        }
      }

      object Server extends autowire.Server with Rw{
        val routes = route[Api](Controller)
      }

      object Client extends autowire.Client with Rw{
        case class NoSuchRoute(msg: String) extends Exception(msg)
        def callRequest(r: Request) = {
          Server.routes
            .lift(r)
            .getOrElse(Future.failed(new NoSuchRoute("No route found : " + r.path)))
        }
      }

      val res1 = await(Client[Api].add(1, 2, 3).call())
      val res2 = await(Client[Api].add(1).call())
      val res3 = await(Client[Api].add(1, 2).call())

      val res4 = await(Client[Api].multiply(x = 1.2, Seq(2.3)).call())
      val res5 = await(Client[Api].multiply(x = 1.1, ys = Seq(2.2, 3.3, 4.4)).call())

      assert(
        res1 == "1+2+3",
        res2 == "1+2+10",
        res3 == "1+2+10",
        res4 == "1.2*2.3",
        res5 == "1.1*2.2*3.3*4.4"
      )
    }
    'pickling {
      trait Rw{
        def write[T: SPickler: FastTypeTag](t: T): String = {
          t.pickle.value
        }
        def read[T: Unpickler: FastTypeTag](s: String): T = {
          JSONPickle(s).unpickle[T]
        }
      }

      object Server extends autowire.Server with Rw{
        val routes = route[Api](Controller)
      }

      object Client extends autowire.Client with Rw{
        case class NoSuchRoute(msg: String) extends Exception(msg)
        def callRequest(r: Request) = {
          Server.routes
            .lift(r)
            .getOrElse(Future.failed(new NoSuchRoute("No route found : " + r.path)))
        }
      }

      val res1 = await(Client[Api].add(1, 2, 3).call())
      val res2 = await(Client[Api].add(1).call())
      val res3 = await(Client[Api].add(1, 2).call())

      val res4 = await(Client[Api].multiply(x = 1.2, Seq(2.3)).call())
      val res5 = await(Client[Api].multiply(x = 1.1, ys = Seq(2.2, 3.3, 4.4)).call())

      assert(
        res1 == "1+2+3",
        res2 == "1+2+10",
        res3 == "1+2+10",
        res4 == "1.2*2.3",
        res5 == "1.1*2.2*3.3*4.4"
      )
    }
  }
}