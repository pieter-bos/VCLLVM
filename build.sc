import $ivy.`me.pieterbos::mill-cpp::0.0.1`

import mill._

import me.pieterbos.mill.cpp._

// def standard = T[options.CppStandard] { options.CppStandard.Cpp14 }

object main extends RootModule with CppExecutableModule { outer =>
	def root: T[os.Path] = T { millSourcePath }

	object llvm extends LinkableModule {
		def moduleDeps = Nil
		def systemLibraryDeps = T { Seq("LLVM-15") }
		def staticObjects = T { Seq.empty[PathRef] }
		def dynamicObjects = T { Seq.empty[PathRef] }
		def exportIncludePaths = T.sources(
			os.Path("/usr/include/llvm-15"), 
			os.Path("/usr/include/llvm-c-15"),
		)
	}

	object json extends LinkableModule {
		def moduleDeps = Nil
		def systemLibraryDeps = T { Seq.empty[String] }
		def staticObjects = T { Seq.empty[PathRef] }
		def dynamicObjects = T { Seq.empty[PathRef] }
		def exportIncludePaths = T {
			os.write(T.dest / "json.tar.xz", requests.get.stream("https://github.com/nlohmann/json/releases/download/v3.11.2/json.tar.xz"))
			os.proc("tar", "-xf", T.dest / "json.tar.xz").call(cwd = T.dest)
			Seq(PathRef(T.dest / "json" / "include"))
		}
	}

	object origin extends CppModule {
		def standard = T[options.CppStandard] { options.CppStandard.Cpp20 }
		def moduleDeps = Seq(llvm, json)
		def sources = T { Seq(PathRef(outer.root() / "lib" / "Origin")) }
		def includePaths = T { Seq(PathRef(outer.root() / "include")) }
	}

	object passes extends CppModule {
		def standard = T[options.CppStandard] { options.CppStandard.Cpp20 }
		def moduleDeps = Seq(llvm, proto.colProto)
		def sources = T { Seq(PathRef(outer.root() / "lib" / "Passes")) }
		def includePaths = T { Seq(PathRef(outer.root() / "include")) }
	}

	object transform extends CppModule {
		def standard = T[options.CppStandard] { options.CppStandard.Cpp20 }
		def moduleDeps = Seq(llvm, proto.colProto)
		def sources = T { Seq(PathRef(outer.root() / "lib" / "Transform")) }
		def includePaths = T { Seq(PathRef(outer.root() / "include")) }
	}

	object util extends CppModule {
		def standard = T[options.CppStandard] { options.CppStandard.Cpp20 }
		def moduleDeps = Seq(llvm)
		def sources = T { Seq(PathRef(outer.root() / "lib" / "Util")) }
		def includePaths = T { Seq(PathRef(outer.root() / "include")) }
	}

	def standard = T[options.CppStandard] { options.CppStandard.Cpp20 }
	def moduleDeps = Seq(origin, passes, transform, util, llvm, proto.colProto)
	def sources = T { Seq(PathRef(root() / "tools" / "VCLLVM")) }
	def includePaths = T { Seq(PathRef(root() / "include")) }	

	object proto extends CMakeModule {
		def root = T.source(os.Path("/home/pieter/protobuf"))
		def jobs = T { 24 }

		object libprotobuf extends CMakeLibrary {
			def target = T { "libprotobuf" }
		}

		object protoc extends CMakeExecutable {
			def target = T { "protoc" }
		}

		def protoPath = T { millSourcePath }
		def proto = T { PathRef(protoPath() / "col.proto") }

		def generate = T {
			os.proc(protoc.executable().path, "-I=" + protoPath().toString,  "--cpp_out=" + T.dest.toString, proto().path).call()
			T.dest
		}

		object colProto extends CppModule {
			def standard = T[options.CppStandard] { options.CppStandard.Cpp20 }
			def moduleDeps = Seq(libprotobuf)
			def sources = T { Seq(PathRef(generate())) }
			def includePaths = T { Seq(PathRef(generate())) }
		}
	}
}