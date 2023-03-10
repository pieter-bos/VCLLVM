#include "Passes/Function/FunctionDeclarer.h"
#include "Util/Conversion.h"
#include "Util/Exceptions.h"


namespace llvm {
    namespace col = vct::col::serialize;

    /*
     * Function Declarer Result
     */

    FDResult::FDResult(col::LlvmFunctionDefinition *colFuncDef) :
            associatedColFuncDef(colFuncDef) {}

    col::LlvmFunctionDefinition *FDResult::getAssociatedColFuncDef() {
        return associatedColFuncDef;
    }

    /*
     * Function Declarer (Analysis)
     */
    AnalysisKey FunctionDeclarer::Key;

    FunctionDeclarer::FunctionDeclarer(std::shared_ptr<col::Program> pProgram) :
            pProgram(std::move(pProgram)) {}

    FDResult FunctionDeclarer::run(Function &F, FunctionAnalysisManager &FAM) {
        // create llvmFuncDef declaration in buffer
        col::GlobalDeclaration *llvmFuncDefDecl = pProgram->add_declarations();
        // generate id
        llvm2Col::setColNodeId(llvmFuncDefDecl);
        col::LlvmFunctionDefinition *llvmFuncDef = llvmFuncDefDecl->mutable_llvm_function_definition();
        return FDResult(llvmFuncDef);
    }

    /*
     * Function Declarer Pass
     */
    FunctionDeclarerPass::FunctionDeclarerPass(std::shared_ptr<col::Program> pProgram) :
            pProgram(std::move(pProgram)) {}

    PreservedAnalyses FunctionDeclarerPass::run(Function &F, FunctionAnalysisManager &FAM) {
        FDResult result = FAM.getResult<FunctionDeclarer>(F);
        col::LlvmFunctionDefinition *colFunction = result.getAssociatedColFuncDef();
        // complete the procedure declaration in proto buffer
        // set return type in protobuf of function
        try {
            llvm2Col::convertAndSetType(F.getReturnType(), colFunction->mutable_return_type());
        } catch (vcllvm::UnsupportedTypeException &e) {
            std::stringstream errorStream;
            errorStream << e.what() << " in return type of function \"" << F.getName().str() << "\"";
            vcllvm::ErrorCollector::addError("Passes::Function::FunctionDeclarer", errorStream.str());
        }
        // set args (if present)
        for (llvm::Argument &llvmArg: F.args()) {
            col::Variable *colArg = colFunction->add_args();
            llvm2Col::setColNodeId(colArg);
            try {
                llvm2Col::convertAndSetType(llvmArg.getType(), colArg->mutable_t());
            } catch (vcllvm::UnsupportedTypeException &e) {
                std::stringstream errorStream;
                errorStream << e.what() << " in argument #" << llvmArg.getArgNo() << " of function \""
                            << F.getName().str() << "\"";
                vcllvm::ErrorCollector::addError("Passes::Function::FunctionDeclarer", errorStream.str());
            }
        }
        //TODO body (separate pass)
        //TODO contract (separate pass)
        return PreservedAnalyses::all();
    }
}
