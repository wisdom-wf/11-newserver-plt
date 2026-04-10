import { useState, useEffect } from 'react';
import { Table, Card, Button, Space, message, Tag, Rate, Modal, Input } from 'antd';
import { ReloadOutlined, SendOutlined, DownloadOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { getEvaluationList, replyEvaluation, exportEvaluations } from '../../api/evaluation';
import type { Evaluation, EvaluationQuery } from '../../types';
import { formatDate } from '../../utils';

const EvaluationList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Evaluation[]>([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [replyModalVisible, setReplyModalVisible] = useState(false);
  const [replyingEvaluation, setReplyingEvaluation] = useState<Evaluation | null>(null);
  const [replyText, setReplyText] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true);
      const response = await getEvaluationList({ page, pageSize } as EvaluationQuery);
      setDataSource(response.data.list);
      setPagination({
        current: response.data.page,
        pageSize: response.data.pageSize,
        total: response.data.total,
      });
    } catch {
      message.error('获取评价列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pag: TablePaginationConfig) => {
    if (pag.current && pag.pageSize) {
      fetchData(pag.current, pag.pageSize);
    }
  };

  const handleReply = (record: Evaluation) => {
    setReplyingEvaluation(record);
    setReplyText(record.reply || '');
    setReplyModalVisible(true);
  };

  const handleSubmitReply = async () => {
    if (!replyingEvaluation) return;
    try {
      await replyEvaluation(replyingEvaluation.id, replyText);
      message.success('回复成功');
      setReplyModalVisible(false);
      fetchData(pagination.current, pagination.pageSize);
    } catch {
      message.error('回复失败');
    }
  };

  const handleExport = async () => {
    try {
      await exportEvaluations();
      message.info('导出功能暂不可用');
    } catch {
      message.error('导出失败');
    }
  };

  const columns: ColumnsType<Evaluation> = [
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
    { title: '老人姓名', dataIndex: 'elderName', key: 'elderName' },
    {
      title: '服务评分',
      dataIndex: 'serviceScore',
      key: 'serviceScore',
      render: (score: number) => <Rate disabled defaultValue={score} />,
    },
    {
      title: '综合评分',
      dataIndex: 'overallScore',
      key: 'overallScore',
      render: (score: number) => <Rate disabled defaultValue={score} />,
    },
    {
      title: '评价内容',
      dataIndex: 'content',
      key: 'content',
      ellipsis: true,
      render: (text: string) => text || '-',
    },
    {
      title: '回复内容',
      dataIndex: 'reply',
      key: 'reply',
      ellipsis: true,
      render: (text: string) => text || <Tag color="orange">待回复</Tag>,
    },
    { title: '评价时间', dataIndex: 'createTime', key: 'createTime', render: (text) => formatDate(text) },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<SendOutlined />} onClick={() => handleReply(record)}>
            回复
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="服务评价管理"
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => fetchData(pagination.current, pagination.pageSize)}>
            刷新
          </Button>
          <Button icon={<DownloadOutlined />} onClick={handleExport}>
            导出
          </Button>
        </Space>
      }
    >
      <Table
        columns={columns}
        dataSource={dataSource}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
        onChange={handleTableChange}
      />

      <Modal
        title="回复评价"
        open={replyModalVisible}
        onOk={handleSubmitReply}
        onCancel={() => setReplyModalVisible(false)}
        width={600}
      >
        <div style={{ marginBottom: 16 }}>
          <strong>评价内容：</strong>
          <p>{replyingEvaluation?.content || '-'}</p>
        </div>
        <Input.TextArea
          rows={4}
          placeholder="请输入回复内容"
          value={replyText}
          onChange={(e) => setReplyText(e.target.value)}
        />
      </Modal>
    </Card>
  );
};

export default EvaluationList;
