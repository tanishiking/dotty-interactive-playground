object Foo {
  def foo[T: Ordering](name: T, age: Int, address: String) = ???
  def bar(name: String = "aaa", age: Int = 23, address: String) = ???
  case class Baz(
      name: String = "test",
      age: Int = 24,
      address: String
  )

}
