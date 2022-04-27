import org.scalatest.funsuite.AnyFunSuite

class FlowSuite extends AnyFunSuite {
  test("2 plus 2") {
    val actual = Flow.plus(2, 2)
    val expected = 4
    assert(actual == expected)
  }
}
