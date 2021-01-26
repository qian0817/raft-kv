package com.qianlei.log

import java.io.File

/**
 *
 * @author qianlei
 */
class NormalLogDir(dir: File) : AbstractLogDir(dir) {
    override fun toString(): String {
        return "NormalLogDir(dir=${dir.name})"
    }
}