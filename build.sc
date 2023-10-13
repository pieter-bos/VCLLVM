import $ivy.`me.pieterbos::me.pieterbos.mill.cpp:0.0.1`

import mill._

import me.pieterbos.mill.cpp._

// def standard = T[options.CppStandard] { options.CppStandard.Cpp14 }

object main extends RootModule with CppModule { outer =>
	def root = T { millSourcePath }
	def sources = T { Seq.empty[PathRef] }
	def moduleDeps = Seq(origin, passes, transform, util)

	def llvmInc: T[Seq[String]] = T { Seq("-I/usr/include/llvm-15/", "-I/usr/include/llvm-c-15/") }
	def jsonInc: T[Seq[String]] = T { Seq("-I" + json.download().path.toString()) }

	object origin extends CppModule {
		def standard = T[options.CppStandard] { options.CppStandard.Cpp20 }
		def sources = T { Seq(PathRef(outer.root() / "lib" / "Origin")) }
		def includePaths = T { Seq(PathRef(outer.root() / "include")) }
		def additionalOptions = T { outer.llvmInc() ++ outer.jsonInc() }
	}

	object passes extends CppModule {
		def standard = T[options.CppStandard] { options.CppStandard.Cpp20 }
		def sources = T { Seq(PathRef(outer.root() / "lib" / "Passes")) }
		def includePaths = T { Seq(PathRef(outer.root() / "include")) }
		def additionalOptions = T { outer.llvmInc() }
		def moduleDeps = Seq(outer.proto)
	}

	object transform extends CppModule {
		def sources = T { Seq(PathRef(outer.root() / "lib" / "Transform")) }
		def includePaths = T { Seq(PathRef(outer.root() / "include")) }
		def additionalOptions = T {
			Seq[String](
				// "-I/usr/include/llvm-15/",
				// "-I/usr/include/llvm-c-15/",
				// "-I" + outer.json.download().path.toString(),
			)
		}
	}

	object util extends CppModule {
		def sources = T { Seq(PathRef(outer.root() / "lib" / "Util")) }
		def includePaths = T { Seq(PathRef(outer.root() / "include")) }
		def additionalOptions = T {
			Seq[String](
				// "-I/usr/include/llvm-15/",
				// "-I/usr/include/llvm-c-15/",
				// "-I" + outer.json.download().path.toString(),
			)
		}
	}

	def additionalOptions = T {
		Seq(
			"-I/usr/include/llvm-15/",
			"-I/usr/include/llvm-c-15/",
			"-I" + json.download().path.toString(),
		)
	}

	object json extends Module {
		def download = T {
			os.write(T.dest / "json.tar.xz", requests.get.stream("https://github.com/nlohmann/json/releases/download/v3.11.2/json.tar.xz"))
			os.proc("tar", "-xf", T.dest / "json.tar.xz").call(cwd = T.dest)
			PathRef(T.dest / "json" / "include")
		}
	}

	object proto extends CppModule {
		def protoPath = T { millSourcePath }
		def proto = T { PathRef(protoPath() / "col.proto") }

		def generate = T {
			os.proc("protoc", "-I=" + protoPath().toString(),  "--cpp_out=" + T.dest.toString(), proto().path).call()
			T.dest
		}

		def standard = T[options.CppStandard] { options.CppStandard.Cpp14 }

		def sources = T {
			Seq(PathRef(generate() / "col.pb.cc"))
		}

		def includePaths = T {
			Seq(PathRef(generate()))
		}
	}
}