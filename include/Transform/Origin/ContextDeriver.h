#ifndef VCLLVM_CONTEXTDERIVER_H
#define VCLLVM_CONTEXTDERIVER_H

#include <llvm/IR/Value.h>

namespace llvm2Col {
    // module derivers
    std::string deriveModuleContext(llvm::Module &llvmModule);

    // function derivers
    std::string deriveFunctionContext(llvm::Function &llvmFunction);

    // block derivers
    std::string deriveLabelContext(llvm::BasicBlock &llvmBlock);

    std::string deriveBlockContext(llvm::BasicBlock &llvmBlock);

    // instruction derivers
    std::string deriveSurroundingInstructionContext(llvm::Instruction &llvmInstruction);

    std::string deriveInstructionContext(llvm::Instruction &llvmInstruction);

    std::string deriveInstructionLhs(llvm::Instruction  &llvmInstruction);

    std::string deriveInstructionRhs(llvm::Instruction &llvmInstruction);

    // operand derivers
    std::string deriveOperandContext(llvm::Value &llvmOperand);
}
#endif //VCLLVM_CONTEXTDERIVER_H
