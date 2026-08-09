/*
 *  VkGraphicsPipelineCreateInfoAccessor.h
 *  Vulkan accessor for VkGraphicsPipelineCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKGRAPHICSPIPELINECREATEINFOACCESSOR_H
#define VKGRAPHICSPIPELINECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkPipelineShaderStageCreateInfoAccessor.h>
#include <includes/VkPipelineVertexInputStateCreateInfoAccessor.h>
#include <includes/VkPipelineInputAssemblyStateCreateInfoAccessor.h>
#include <includes/VkPipelineTessellationStateCreateInfoAccessor.h>
#include <includes/VkPipelineViewportStateCreateInfoAccessor.h>
#include <includes/VkPipelineRasterizationStateCreateInfoAccessor.h>
#include <includes/VkPipelineMultisampleStateCreateInfoAccessor.h>
#include <includes/VkPipelineDepthStencilStateCreateInfoAccessor.h>
#include <includes/VkPipelineColorBlendStateCreateInfoAccessor.h>
#include <includes/VkPipelineDynamicStateCreateInfoAccessor.h>

class VkGraphicsPipelineCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pStagesField;
    jfieldID pVertexInputStateField;
    jfieldID pInputAssemblyStateField;
    jfieldID pTessellationStateField;
    jfieldID pViewportStateField;
    jfieldID pRasterizationStateField;
    jfieldID pMultisampleStateField;
    jfieldID pDepthStencilStateField;
    jfieldID pColorBlendStateField;
    jfieldID pDynamicStateField;
    jfieldID layoutField;
    jfieldID renderPassField;
    jfieldID subpassField;
    jfieldID basePipelineHandleField;
    jfieldID basePipelineIndexField;
public:
    VkGraphicsPipelineCreateInfoAccessor(JNIEnv *env, jobject obj);

    void getpStages(VkGraphicsPipelineCreateInfo &clazzInfo);

    VkPipeline getbasePipelineHandle();

    void getpTessellationState(VkGraphicsPipelineCreateInfo &clazzInfo);

    void getpRasterizationState(VkGraphicsPipelineCreateInfo &clazzInfo);

    void getpDepthStencilState(VkGraphicsPipelineCreateInfo &clazzInfo);

    uint32_t getsubpass();

    VkRenderPass getrenderPass();

    uint32_t getbasePipelineIndex();

    VkStructureType getsType();

    uint32_t getflags();

    void getpViewportState(VkGraphicsPipelineCreateInfo &clazzInfo);

    void getpInputAssemblyState(VkGraphicsPipelineCreateInfo &clazzInfo);

    void getpColorBlendState(VkGraphicsPipelineCreateInfo &clazzInfo);

    VkPipelineLayout getlayout();

    void getpNext(VkGraphicsPipelineCreateInfo &clazzInfo);

    void fromObject(VkGraphicsPipelineCreateInfo &clazzInfo);

    void getpVertexInputState(VkGraphicsPipelineCreateInfo &clazzInfo);

    void getpDynamicState(VkGraphicsPipelineCreateInfo &clazzInfo);

    void getpMultisampleState(VkGraphicsPipelineCreateInfo &clazzInfo);

    ~VkGraphicsPipelineCreateInfoAccessor();
};
#endif // VKGRAPHICSPIPELINECREATEINFOACCESSOR_H