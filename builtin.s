	.text
	.file	"t.c"
	.globl	_malloc_and_init        # -- Begin function _malloc_and_init
	.p2align	2
	.type	_malloc_and_init,@function
_malloc_and_init:                       # @_malloc_and_init
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	addi	s0, sp, 32
	sw	a0, -12(s0)
	lw	a0, -12(s0)
	call	malloc
	sw	a0, -16(s0)
	lw	a0, -16(s0)
	lw	a2, -12(s0)
	mv	a1, zero
	call	memset
	lw	a1, -16(s0)
	sw	a0, -20(s0)
	mv	a0, a1
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end0:
	.size	_malloc_and_init, .Lfunc_end0-_malloc_and_init
                                        # -- End function
	.globl	print                   # -- Begin function print
	.p2align	2
	.type	print,@function
print:                                  # @print
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	addi	s0, sp, 16
	sw	a0, -12(s0)
	lw	a1, -12(s0)
	lui	a0, %hi(.L.str)
	addi	a0, a0, %lo(.L.str)
	call	printf
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end1:
	.size	print, .Lfunc_end1-print
                                        # -- End function
	.globl	println                 # -- Begin function println
	.p2align	2
	.type	println,@function
println:                                # @println
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	addi	s0, sp, 16
	sw	a0, -12(s0)
	lw	a1, -12(s0)
	lui	a0, %hi(.L.str.1)
	addi	a0, a0, %lo(.L.str.1)
	call	printf
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end2:
	.size	println, .Lfunc_end2-println
                                        # -- End function
	.globl	printInt                # -- Begin function printInt
	.p2align	2
	.type	printInt,@function
printInt:                               # @printInt
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	addi	s0, sp, 16
	sw	a0, -12(s0)
	lw	a1, -12(s0)
	lui	a0, %hi(.L.str.2)
	addi	a0, a0, %lo(.L.str.2)
	call	printf
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end3:
	.size	printInt, .Lfunc_end3-printInt
                                        # -- End function
	.globl	printlnInt              # -- Begin function printlnInt
	.p2align	2
	.type	printlnInt,@function
printlnInt:                             # @printlnInt
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	addi	s0, sp, 16
	sw	a0, -12(s0)
	lw	a1, -12(s0)
	lui	a0, %hi(.L.str.3)
	addi	a0, a0, %lo(.L.str.3)
	call	printf
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end4:
	.size	printlnInt, .Lfunc_end4-printlnInt
                                        # -- End function
	.globl	getString               # -- Begin function getString
	.p2align	2
	.type	getString,@function
getString:                              # @getString
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	addi	s0, sp, 16
	addi	a0, zero, 257
	call	malloc
	sw	a0, -12(s0)
	lw	a1, -12(s0)
	lui	a0, %hi(.L.str)
	addi	a0, a0, %lo(.L.str)
	call	scanf
	lw	a1, -12(s0)
	sw	a0, -16(s0)
	mv	a0, a1
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end5:
	.size	getString, .Lfunc_end5-getString
                                        # -- End function
	.globl	getInt                  # -- Begin function getInt
	.p2align	2
	.type	getInt,@function
getInt:                                 # @getInt
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	addi	s0, sp, 16
	lui	a0, %hi(.L.str.2)
	addi	a0, a0, %lo(.L.str.2)
	addi	a1, s0, -12
	call	scanf
	lw	a1, -12(s0)
	sw	a0, -16(s0)
	mv	a0, a1
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end6:
	.size	getInt, .Lfunc_end6-getInt
                                        # -- End function
	.globl	toString                # -- Begin function toString
	.p2align	2
	.type	toString,@function
toString:                               # @toString
# %bb.0:
	addi	sp, sp, -48
	sw	ra, 44(sp)
	sw	s0, 40(sp)
	addi	s0, sp, 48
	sw	a0, -16(s0)
	lw	a0, -16(s0)
	mv	a1, zero
	bne	a0, a1, .LBB7_2
	j	.LBB7_1
.LBB7_1:
	addi	a0, zero, 2
	call	malloc
	sw	a0, -20(s0)
	lw	a0, -20(s0)
	addi	a1, zero, 48
	sb	a1, 0(a0)
	lw	a0, -20(s0)
	mv	a1, zero
	sb	a1, 1(a0)
	lw	a0, -20(s0)
	sw	a0, -12(s0)
	j	.LBB7_14
.LBB7_2:
	mv	a0, zero
	sb	a0, -32(s0)
	lw	a0, -16(s0)
	addi	a1, zero, 1
	blt	a0, a1, .LBB7_4
	j	.LBB7_3
.LBB7_3:
	mv	a0, zero
	sb	a0, -31(s0)
	j	.LBB7_5
.LBB7_4:
	addi	a0, zero, 1
	sb	a0, -31(s0)
	lw	a0, -16(s0)
	mv	a1, zero
	sub	a0, a1, a0
	sw	a0, -16(s0)
	j	.LBB7_5
.LBB7_5:
	j	.LBB7_6
.LBB7_6:                                # =>This Inner Loop Header: Depth=1
	lw	a0, -16(s0)
	addi	a1, zero, 1
	blt	a0, a1, .LBB7_8
	j	.LBB7_7
.LBB7_7:                                #   in Loop: Header=BB7_6 Depth=1
	lw	a0, -16(s0)
	lui	a1, 419430
	addi	a1, a1, 1639
	mulh	a2, a0, a1
	srli	a3, a2, 31
	srli	a2, a2, 2
	add	a2, a2, a3
	addi	a3, zero, 10
	mul	a2, a2, a3
	sub	a0, a0, a2
	lbu	a2, -32(s0)
	addi	a3, a2, 1
	sb	a3, -32(s0)
	addi	a3, s0, -30
	add	a2, a3, a2
	sb	a0, 0(a2)
	lw	a0, -16(s0)
	mulh	a0, a0, a1
	srli	a1, a0, 31
	srai	a0, a0, 2
	add	a0, a0, a1
	sw	a0, -16(s0)
	j	.LBB7_6
.LBB7_8:
	lbu	a0, -31(s0)
	lbu	a1, -32(s0)
	add	a0, a0, a1
	addi	a0, a0, 1
	call	malloc
	sw	a0, -36(s0)
	lbu	a0, -31(s0)
	addi	a1, zero, 1
	blt	a0, a1, .LBB7_10
	j	.LBB7_9
.LBB7_9:
	lw	a0, -36(s0)
	addi	a1, zero, 45
	sb	a1, 0(a0)
	j	.LBB7_10
.LBB7_10:
	mv	a0, zero
	sb	a0, -37(s0)
	j	.LBB7_11
.LBB7_11:                               # =>This Inner Loop Header: Depth=1
	lbu	a0, -37(s0)
	lbu	a1, -32(s0)
	bge	a0, a1, .LBB7_13
	j	.LBB7_12
.LBB7_12:                               #   in Loop: Header=BB7_11 Depth=1
	lbu	a0, -32(s0)
	lbu	a1, -37(s0)
	sub	a0, a0, a1
	addi	a2, s0, -30
	add	a0, a0, a2
	lb	a0, -1(a0)
	addi	a0, a0, 48
	lw	a2, -36(s0)
	lbu	a3, -31(s0)
	add	a1, a1, a3
	add	a1, a2, a1
	sb	a0, 0(a1)
	lb	a0, -37(s0)
	addi	a0, a0, 1
	sb	a0, -37(s0)
	j	.LBB7_11
.LBB7_13:
	lw	a0, -36(s0)
	lbu	a1, -32(s0)
	lbu	a2, -31(s0)
	add	a1, a1, a2
	add	a0, a0, a1
	mv	a1, zero
	sb	a1, 0(a0)
	lw	a0, -36(s0)
	sw	a0, -12(s0)
	j	.LBB7_14
.LBB7_14:
	lw	a0, -12(s0)
	lw	s0, 40(sp)
	lw	ra, 44(sp)
	addi	sp, sp, 48
	ret
.Lfunc_end7:
	.size	toString, .Lfunc_end7-toString
                                        # -- End function
	.globl	__string_concatenate    # -- Begin function __string_concatenate
	.p2align	2
	.type	__string_concatenate,@function
__string_concatenate:                   # @__string_concatenate
# %bb.0:
	addi	sp, sp, -48
	sw	ra, 44(sp)
	sw	s0, 40(sp)
	addi	s0, sp, 48
	sw	a0, -12(s0)
	sw	a1, -16(s0)
	mv	a0, zero
	sw	a0, -20(s0)
	sw	a0, -24(s0)
	j	.LBB8_1
.LBB8_1:                                # =>This Inner Loop Header: Depth=1
	lw	a0, -12(s0)
	lw	a1, -20(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB8_3
	j	.LBB8_2
.LBB8_2:                                #   in Loop: Header=BB8_1 Depth=1
	lw	a0, -20(s0)
	addi	a0, a0, 1
	sw	a0, -20(s0)
	j	.LBB8_1
.LBB8_3:
	j	.LBB8_4
.LBB8_4:                                # =>This Inner Loop Header: Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB8_6
	j	.LBB8_5
.LBB8_5:                                #   in Loop: Header=BB8_4 Depth=1
	lw	a0, -24(s0)
	addi	a0, a0, 1
	sw	a0, -24(s0)
	j	.LBB8_4
.LBB8_6:
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	addi	a0, a0, 1
	call	malloc
	sw	a0, -28(s0)
	mv	a0, zero
	sw	a0, -32(s0)
	sw	a0, -36(s0)
	j	.LBB8_7
.LBB8_7:                                # =>This Inner Loop Header: Depth=1
	lw	a0, -36(s0)
	lw	a1, -20(s0)
	bge	a0, a1, .LBB8_9
	j	.LBB8_8
.LBB8_8:                                #   in Loop: Header=BB8_7 Depth=1
	lw	a0, -12(s0)
	lw	a1, -36(s0)
	addi	a2, a1, 1
	sw	a2, -36(s0)
	add	a0, a0, a1
	lb	a0, 0(a0)
	lw	a1, -28(s0)
	lw	a2, -32(s0)
	addi	a3, a2, 1
	sw	a3, -32(s0)
	add	a1, a1, a2
	sb	a0, 0(a1)
	j	.LBB8_7
.LBB8_9:
	mv	a0, zero
	sw	a0, -36(s0)
	j	.LBB8_10
.LBB8_10:                               # =>This Inner Loop Header: Depth=1
	lw	a0, -36(s0)
	lw	a1, -24(s0)
	bge	a0, a1, .LBB8_12
	j	.LBB8_11
.LBB8_11:                               #   in Loop: Header=BB8_10 Depth=1
	lw	a0, -16(s0)
	lw	a1, -36(s0)
	addi	a2, a1, 1
	sw	a2, -36(s0)
	add	a0, a0, a1
	lb	a0, 0(a0)
	lw	a1, -28(s0)
	lw	a2, -32(s0)
	addi	a3, a2, 1
	sw	a3, -32(s0)
	add	a1, a1, a2
	sb	a0, 0(a1)
	j	.LBB8_10
.LBB8_12:
	lw	a0, -28(s0)
	lw	a1, -32(s0)
	add	a0, a0, a1
	mv	a1, zero
	sb	a1, 0(a0)
	lw	a0, -28(s0)
	lw	s0, 40(sp)
	lw	ra, 44(sp)
	addi	sp, sp, 48
	ret
.Lfunc_end8:
	.size	__string_concatenate, .Lfunc_end8-__string_concatenate
                                        # -- End function
	.globl	__string_equal          # -- Begin function __string_equal
	.p2align	2
	.type	__string_equal,@function
__string_equal:                         # @__string_equal
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	addi	s0, sp, 32
	sw	a0, -16(s0)
	sw	a1, -20(s0)
	mv	a0, zero
	sw	a0, -24(s0)
	j	.LBB9_1
.LBB9_1:                                # =>This Inner Loop Header: Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	mv	a2, a1
	sw	a2, -28(s0)
	beq	a0, a1, .LBB9_3
	j	.LBB9_2
.LBB9_2:                                #   in Loop: Header=BB9_1 Depth=1
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	snez	a0, a0
	sw	a0, -28(s0)
	j	.LBB9_3
.LBB9_3:                                #   in Loop: Header=BB9_1 Depth=1
	lw	a0, -28(s0)
	andi	a0, a0, 1
	mv	a1, zero
	beq	a0, a1, .LBB9_7
	j	.LBB9_4
.LBB9_4:                                #   in Loop: Header=BB9_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	beq	a0, a1, .LBB9_6
	j	.LBB9_5
.LBB9_5:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB9_8
.LBB9_6:                                #   in Loop: Header=BB9_1 Depth=1
	lw	a0, -24(s0)
	addi	a0, a0, 1
	sw	a0, -24(s0)
	j	.LBB9_1
.LBB9_7:
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	xor	a0, a0, a1
	seqz	a0, a0
	sb	a0, -9(s0)
	j	.LBB9_8
.LBB9_8:
	lbu	a0, -9(s0)
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end9:
	.size	__string_equal, .Lfunc_end9-__string_equal
                                        # -- End function
	.globl	__string_notEqual       # -- Begin function __string_notEqual
	.p2align	2
	.type	__string_notEqual,@function
__string_notEqual:                      # @__string_notEqual
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	addi	s0, sp, 32
	sw	a0, -16(s0)
	sw	a1, -20(s0)
	mv	a0, zero
	sw	a0, -24(s0)
	j	.LBB10_1
.LBB10_1:                               # =>This Inner Loop Header: Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	mv	a2, a1
	sw	a2, -28(s0)
	beq	a0, a1, .LBB10_3
	j	.LBB10_2
.LBB10_2:                               #   in Loop: Header=BB10_1 Depth=1
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	snez	a0, a0
	sw	a0, -28(s0)
	j	.LBB10_3
.LBB10_3:                               #   in Loop: Header=BB10_1 Depth=1
	lw	a0, -28(s0)
	andi	a0, a0, 1
	mv	a1, zero
	beq	a0, a1, .LBB10_7
	j	.LBB10_4
.LBB10_4:                               #   in Loop: Header=BB10_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	beq	a0, a1, .LBB10_6
	j	.LBB10_5
.LBB10_5:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB10_8
.LBB10_6:                               #   in Loop: Header=BB10_1 Depth=1
	lw	a0, -24(s0)
	addi	a0, a0, 1
	sw	a0, -24(s0)
	j	.LBB10_1
.LBB10_7:
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	xor	a0, a0, a1
	snez	a0, a0
	sb	a0, -9(s0)
	j	.LBB10_8
.LBB10_8:
	lbu	a0, -9(s0)
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end10:
	.size	__string_notEqual, .Lfunc_end10-__string_notEqual
                                        # -- End function
	.globl	__string_lessThan       # -- Begin function __string_lessThan
	.p2align	2
	.type	__string_lessThan,@function
__string_lessThan:                      # @__string_lessThan
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	addi	s0, sp, 32
	sw	a0, -16(s0)
	sw	a1, -20(s0)
	mv	a0, zero
	sw	a0, -24(s0)
	j	.LBB11_1
.LBB11_1:                               # =>This Inner Loop Header: Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	mv	a2, a1
	sw	a2, -28(s0)
	beq	a0, a1, .LBB11_3
	j	.LBB11_2
.LBB11_2:                               #   in Loop: Header=BB11_1 Depth=1
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	snez	a0, a0
	sw	a0, -28(s0)
	j	.LBB11_3
.LBB11_3:                               #   in Loop: Header=BB11_1 Depth=1
	lw	a0, -28(s0)
	andi	a0, a0, 1
	mv	a1, zero
	beq	a0, a1, .LBB11_10
	j	.LBB11_4
.LBB11_4:                               #   in Loop: Header=BB11_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	bge	a0, a1, .LBB11_6
	j	.LBB11_5
.LBB11_5:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB11_15
.LBB11_6:                               #   in Loop: Header=BB11_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	bge	a1, a0, .LBB11_8
	j	.LBB11_7
.LBB11_7:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB11_15
.LBB11_8:                               #   in Loop: Header=BB11_1 Depth=1
	j	.LBB11_9
.LBB11_9:                               #   in Loop: Header=BB11_1 Depth=1
	lw	a0, -24(s0)
	addi	a0, a0, 1
	sw	a0, -24(s0)
	j	.LBB11_1
.LBB11_10:
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB11_12
	j	.LBB11_11
.LBB11_11:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB11_15
.LBB11_12:
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB11_14
	j	.LBB11_13
.LBB11_13:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB11_15
.LBB11_14:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB11_15
.LBB11_15:
	lbu	a0, -9(s0)
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end11:
	.size	__string_lessThan, .Lfunc_end11-__string_lessThan
                                        # -- End function
	.globl	__string_greaterThan    # -- Begin function __string_greaterThan
	.p2align	2
	.type	__string_greaterThan,@function
__string_greaterThan:                   # @__string_greaterThan
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	addi	s0, sp, 32
	sw	a0, -16(s0)
	sw	a1, -20(s0)
	mv	a0, zero
	sw	a0, -24(s0)
	j	.LBB12_1
.LBB12_1:                               # =>This Inner Loop Header: Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	mv	a2, a1
	sw	a2, -28(s0)
	beq	a0, a1, .LBB12_3
	j	.LBB12_2
.LBB12_2:                               #   in Loop: Header=BB12_1 Depth=1
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	snez	a0, a0
	sw	a0, -28(s0)
	j	.LBB12_3
.LBB12_3:                               #   in Loop: Header=BB12_1 Depth=1
	lw	a0, -28(s0)
	andi	a0, a0, 1
	mv	a1, zero
	beq	a0, a1, .LBB12_10
	j	.LBB12_4
.LBB12_4:                               #   in Loop: Header=BB12_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	bge	a0, a1, .LBB12_6
	j	.LBB12_5
.LBB12_5:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB12_15
.LBB12_6:                               #   in Loop: Header=BB12_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	bge	a1, a0, .LBB12_8
	j	.LBB12_7
.LBB12_7:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB12_15
.LBB12_8:                               #   in Loop: Header=BB12_1 Depth=1
	j	.LBB12_9
.LBB12_9:                               #   in Loop: Header=BB12_1 Depth=1
	lw	a0, -24(s0)
	addi	a0, a0, 1
	sw	a0, -24(s0)
	j	.LBB12_1
.LBB12_10:
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB12_12
	j	.LBB12_11
.LBB12_11:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB12_15
.LBB12_12:
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB12_14
	j	.LBB12_13
.LBB12_13:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB12_15
.LBB12_14:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB12_15
.LBB12_15:
	lbu	a0, -9(s0)
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end12:
	.size	__string_greaterThan, .Lfunc_end12-__string_greaterThan
                                        # -- End function
	.globl	__string_lessEqual      # -- Begin function __string_lessEqual
	.p2align	2
	.type	__string_lessEqual,@function
__string_lessEqual:                     # @__string_lessEqual
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	addi	s0, sp, 32
	sw	a0, -16(s0)
	sw	a1, -20(s0)
	mv	a0, zero
	sw	a0, -24(s0)
	j	.LBB13_1
.LBB13_1:                               # =>This Inner Loop Header: Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	mv	a2, a1
	sw	a2, -28(s0)
	beq	a0, a1, .LBB13_3
	j	.LBB13_2
.LBB13_2:                               #   in Loop: Header=BB13_1 Depth=1
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	snez	a0, a0
	sw	a0, -28(s0)
	j	.LBB13_3
.LBB13_3:                               #   in Loop: Header=BB13_1 Depth=1
	lw	a0, -28(s0)
	andi	a0, a0, 1
	mv	a1, zero
	beq	a0, a1, .LBB13_10
	j	.LBB13_4
.LBB13_4:                               #   in Loop: Header=BB13_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	bge	a0, a1, .LBB13_6
	j	.LBB13_5
.LBB13_5:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB13_15
.LBB13_6:                               #   in Loop: Header=BB13_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	bge	a1, a0, .LBB13_8
	j	.LBB13_7
.LBB13_7:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB13_15
.LBB13_8:                               #   in Loop: Header=BB13_1 Depth=1
	j	.LBB13_9
.LBB13_9:                               #   in Loop: Header=BB13_1 Depth=1
	lw	a0, -24(s0)
	addi	a0, a0, 1
	sw	a0, -24(s0)
	j	.LBB13_1
.LBB13_10:
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB13_12
	j	.LBB13_11
.LBB13_11:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB13_15
.LBB13_12:
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB13_14
	j	.LBB13_13
.LBB13_13:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB13_15
.LBB13_14:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB13_15
.LBB13_15:
	lbu	a0, -9(s0)
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end13:
	.size	__string_lessEqual, .Lfunc_end13-__string_lessEqual
                                        # -- End function
	.globl	__string_greaterEqual   # -- Begin function __string_greaterEqual
	.p2align	2
	.type	__string_greaterEqual,@function
__string_greaterEqual:                  # @__string_greaterEqual
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	addi	s0, sp, 32
	sw	a0, -16(s0)
	sw	a1, -20(s0)
	mv	a0, zero
	sw	a0, -24(s0)
	j	.LBB14_1
.LBB14_1:                               # =>This Inner Loop Header: Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	mv	a2, a1
	sw	a2, -28(s0)
	beq	a0, a1, .LBB14_3
	j	.LBB14_2
.LBB14_2:                               #   in Loop: Header=BB14_1 Depth=1
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	snez	a0, a0
	sw	a0, -28(s0)
	j	.LBB14_3
.LBB14_3:                               #   in Loop: Header=BB14_1 Depth=1
	lw	a0, -28(s0)
	andi	a0, a0, 1
	mv	a1, zero
	beq	a0, a1, .LBB14_10
	j	.LBB14_4
.LBB14_4:                               #   in Loop: Header=BB14_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	bge	a0, a1, .LBB14_6
	j	.LBB14_5
.LBB14_5:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB14_15
.LBB14_6:                               #   in Loop: Header=BB14_1 Depth=1
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	a2, -20(s0)
	add	a1, a2, a1
	lbu	a1, 0(a1)
	bge	a1, a0, .LBB14_8
	j	.LBB14_7
.LBB14_7:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB14_15
.LBB14_8:                               #   in Loop: Header=BB14_1 Depth=1
	j	.LBB14_9
.LBB14_9:                               #   in Loop: Header=BB14_1 Depth=1
	lw	a0, -24(s0)
	addi	a0, a0, 1
	sw	a0, -24(s0)
	j	.LBB14_1
.LBB14_10:
	lw	a0, -16(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB14_12
	j	.LBB14_11
.LBB14_11:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB14_15
.LBB14_12:
	lw	a0, -20(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB14_14
	j	.LBB14_13
.LBB14_13:
	mv	a0, zero
	sb	a0, -9(s0)
	j	.LBB14_15
.LBB14_14:
	addi	a0, zero, 1
	sb	a0, -9(s0)
	j	.LBB14_15
.LBB14_15:
	lbu	a0, -9(s0)
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end14:
	.size	__string_greaterEqual, .Lfunc_end14-__string_greaterEqual
                                        # -- End function
	.globl	__string_length         # -- Begin function __string_length
	.p2align	2
	.type	__string_length,@function
__string_length:                        # @__string_length
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	addi	s0, sp, 16
	sw	a0, -12(s0)
	mv	a0, zero
	sw	a0, -16(s0)
	j	.LBB15_1
.LBB15_1:                               # =>This Inner Loop Header: Depth=1
	lw	a0, -12(s0)
	lw	a1, -16(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	beq	a0, a1, .LBB15_3
	j	.LBB15_2
.LBB15_2:                               #   in Loop: Header=BB15_1 Depth=1
	lw	a0, -16(s0)
	addi	a0, a0, 1
	sw	a0, -16(s0)
	j	.LBB15_1
.LBB15_3:
	lw	a0, -16(s0)
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end15:
	.size	__string_length, .Lfunc_end15-__string_length
                                        # -- End function
	.globl	__string_substring      # -- Begin function __string_substring
	.p2align	2
	.type	__string_substring,@function
__string_substring:                     # @__string_substring
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	addi	s0, sp, 32
	sw	a0, -12(s0)
	sw	a1, -16(s0)
	sw	a2, -20(s0)
	lw	a0, -20(s0)
	lw	a1, -16(s0)
	sub	a0, a0, a1
	sw	a0, -24(s0)
	lw	a0, -24(s0)
	addi	a0, a0, 1
	call	malloc
	sw	a0, -28(s0)
	mv	a0, zero
	sw	a0, -32(s0)
	j	.LBB16_1
.LBB16_1:                               # =>This Inner Loop Header: Depth=1
	lw	a0, -32(s0)
	lw	a1, -24(s0)
	bge	a0, a1, .LBB16_3
	j	.LBB16_2
.LBB16_2:                               #   in Loop: Header=BB16_1 Depth=1
	lw	a0, -12(s0)
	lw	a1, -16(s0)
	lw	a2, -32(s0)
	add	a1, a1, a2
	add	a0, a0, a1
	lb	a0, 0(a0)
	lw	a1, -28(s0)
	add	a1, a1, a2
	sb	a0, 0(a1)
	lw	a0, -32(s0)
	addi	a0, a0, 1
	sw	a0, -32(s0)
	j	.LBB16_1
.LBB16_3:
	lw	a0, -28(s0)
	lw	a1, -24(s0)
	add	a0, a0, a1
	mv	a1, zero
	sb	a1, 0(a0)
	lw	a0, -28(s0)
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end16:
	.size	__string_substring, .Lfunc_end16-__string_substring
                                        # -- End function
	.globl	__string_parseInt       # -- Begin function __string_parseInt
	.p2align	2
	.type	__string_parseInt,@function
__string_parseInt:                      # @__string_parseInt
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	addi	s0, sp, 32
	sw	a0, -12(s0)
	mv	a0, zero
	sw	a0, -16(s0)
	sw	a0, -20(s0)
	j	.LBB17_1
.LBB17_1:                               # =>This Inner Loop Header: Depth=1
	lw	a0, -12(s0)
	lw	a1, -20(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	mv	a2, a1
	sw	a2, -24(s0)
	beq	a0, a1, .LBB17_4
	j	.LBB17_2
.LBB17_2:                               #   in Loop: Header=BB17_1 Depth=1
	lw	a0, -12(s0)
	lw	a1, -20(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	mv	a1, zero
	addi	a2, zero, 48
	sw	a1, -24(s0)
	blt	a0, a2, .LBB17_4
	j	.LBB17_3
.LBB17_3:                               #   in Loop: Header=BB17_1 Depth=1
	lw	a0, -12(s0)
	lw	a1, -20(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	slti	a0, a0, 58
	sw	a0, -24(s0)
	j	.LBB17_4
.LBB17_4:                               #   in Loop: Header=BB17_1 Depth=1
	lw	a0, -24(s0)
	andi	a0, a0, 1
	mv	a1, zero
	beq	a0, a1, .LBB17_6
	j	.LBB17_5
.LBB17_5:                               #   in Loop: Header=BB17_1 Depth=1
	lw	a0, -16(s0)
	addi	a1, zero, 10
	mul	a0, a0, a1
	lw	a1, -12(s0)
	lw	a2, -20(s0)
	addi	a3, a2, 1
	sw	a3, -20(s0)
	add	a1, a1, a2
	lbu	a1, 0(a1)
	add	a0, a0, a1
	addi	a0, a0, -48
	sw	a0, -16(s0)
	j	.LBB17_1
.LBB17_6:
	lw	a0, -16(s0)
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end17:
	.size	__string_parseInt, .Lfunc_end17-__string_parseInt
                                        # -- End function
	.globl	__string_ord            # -- Begin function __string_ord
	.p2align	2
	.type	__string_ord,@function
__string_ord:                           # @__string_ord
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	addi	s0, sp, 16
	sw	a0, -12(s0)
	sw	a1, -16(s0)
	lw	a0, -12(s0)
	lw	a1, -16(s0)
	add	a0, a0, a1
	lbu	a0, 0(a0)
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end18:
	.size	__string_ord, .Lfunc_end18-__string_ord
                                        # -- End function
	.globl	__array_size            # -- Begin function __array_size
	.p2align	2
	.type	__array_size,@function
__array_size:                           # @__array_size
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	addi	s0, sp, 16
	sw	a0, -12(s0)
	lw	a0, -12(s0)
	lw	a0, -8(a0)
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end19:
	.size	__array_size, .Lfunc_end19-__array_size
                                        # -- End function
	.type	.L.str,@object          # @.str
	.section	.rodata.str1.1,"aMS",@progbits,1
.L.str:
	.asciz	"%s"
	.size	.L.str, 3

	.type	.L.str.1,@object        # @.str.1
.L.str.1:
	.asciz	"%s\n"
	.size	.L.str.1, 4

	.type	.L.str.2,@object        # @.str.2
.L.str.2:
	.asciz	"%d"
	.size	.L.str.2, 3

	.type	.L.str.3,@object        # @.str.3
.L.str.3:
	.asciz	"%d\n"
	.size	.L.str.3, 4


	.ident	"clang version 9.0.1 "
	.section	".note.GNU-stack","",@progbits
	.addrsig
	.addrsig_sym malloc
	.addrsig_sym printf
	.addrsig_sym scanf
